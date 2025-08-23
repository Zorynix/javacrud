package crudjava.crudjava.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import crudjava.crudjava.config.RabbitConfig;
import crudjava.crudjava.dto.CreateOrderRequestDTO;
import crudjava.crudjava.dto.OrderDTO;
import crudjava.crudjava.dto.OrderEventDto;
import crudjava.crudjava.dto.OrderItemRequestDTO;
import crudjava.crudjava.exception.CustomerNotFoundException;
import crudjava.crudjava.exception.InsufficientStockException;
import crudjava.crudjava.exception.OrderNotFoundException;
import crudjava.crudjava.exception.ProductNotFoundException;
import crudjava.crudjava.model.Customer;
import crudjava.crudjava.model.Order;
import crudjava.crudjava.model.OrderItem;
import crudjava.crudjava.model.Product;
import crudjava.crudjava.repository.CustomerRepository;
import crudjava.crudjava.repository.OrderRepository;
import crudjava.crudjava.repository.ProductRepository;
import crudjava.crudjava.util.UrlUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;
    private final InventoryService inventoryService;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository,
                       ProductRepository productRepository, RabbitTemplate rabbitTemplate,
                       InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.inventoryService = inventoryService;
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    public OrderDTO createOrder(CreateOrderRequestDTO request) {
        logger.info("Creating new order for customer ID: {}", request.getCustomerId());
        
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + request.getCustomerId()));

        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order(orderNumber, customer);
        order = orderRepository.save(order);

        order.setOrderItems(new ArrayList<>());

        for (OrderItemRequestDTO itemRequest : request.getOrderItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem(order, product, itemRequest.getQuantity(), product.getPrice());
            if (itemRequest.getDiscountAmount() != null) {
                orderItem.setDiscountAmount(itemRequest.getDiscountAmount());
            }

            order.getOrderItems().add(orderItem);

            inventoryService.reserveInventory(product.getId(), itemRequest.getQuantity(),
                    "Order reservation: " + orderNumber);
        }

        order.calculateTotalAmount();
        order = orderRepository.save(order);

        publishOrderEvent(order, "ORDER_CREATED");

        logger.info("Successfully created order with ID: {} and number: {}", order.getId(), order.getOrderNumber());
        return new OrderDTO(order);
    }

    public OrderDTO createOrderFallback(CreateOrderRequestDTO request, Exception ex) {
        logger.error("Order creation failed for customer {}: {}", request.getCustomerId(), ex.getMessage());
        throw new RuntimeException("Order service temporarily unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "orderService")
    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
        logger.info("Updating order {} status to {}", orderId, newStatus);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        String oldStatus = order.getStatus();
        order.setStatus(newStatus);

        switch (newStatus) {
            case "SHIPPED":
                order.setShippedAt(LocalDateTime.now());
                break;
            case "DELIVERED":
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case "CANCELLED":
                final String orderNumber = order.getOrderNumber();
                order.getOrderItems().forEach(item ->
                    inventoryService.releaseInventory(item.getProduct().getId(),
                            item.getQuantity(), "Order cancelled: " + orderNumber));
                break;
        }

        order = orderRepository.save(order);

        publishOrderStatusChangeEvent(order, oldStatus, newStatus);

        logger.info("Order {} status changed from {} to {}", order.getOrderNumber(), oldStatus, newStatus);
        return new OrderDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> findAll(Pageable pageable) {
        logger.info("Finding all orders with pagination");
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(OrderDTO::new);
    }

    @Transactional(readOnly = true)
    public Optional<OrderDTO> findById(Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    logger.info("Found order with ID: {}", id);
                    return new OrderDTO(order);
                });
    }

    @Transactional(readOnly = true)
    public Optional<OrderDTO> findByOrderNumber(String orderNumber) {
        String decodedOrderNumber = UrlUtils.autoDecodeIfNeeded(orderNumber);
        logger.info("Searching order by number: {}", decodedOrderNumber);
        return orderRepository.findByOrderNumber(decodedOrderNumber)
                .map(order -> {
                    logger.info("Found order with number: {}", decodedOrderNumber);
                    return new OrderDTO(order);
                });
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> findOrdersByCustomer(Long customerId, Pageable pageable) {
        logger.info("Finding orders for customer: {}", customerId);
        Page<Order> orders = orderRepository.findByCustomerIdAndStatus(customerId, null, pageable);
        return orders.map(OrderDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        logger.info("Finding orders between {} and {}", startDate, endDate);
        Page<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate, pageable);
        return orders.map(OrderDTO::new);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> findHighValueOrders(BigDecimal minAmount) {
        logger.info("Finding high value orders with min amount: {}", minAmount);
        List<String> statuses = List.of("CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED");
        List<Order> orders = orderRepository.findHighValueOrders(minAmount, statuses);
        return orders.stream().map(OrderDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getDailySalesReport(LocalDateTime startDate) {
        logger.info("Generating daily sales report since: {}", startDate);
        return orderRepository.getDailySalesReport(startDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Calculating total revenue between {} and {}", startDate, endDate);
        BigDecimal revenue = orderRepository.getTotalRevenueByPeriod(startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    private void publishOrderEvent(Order order, String eventType) {
        try {
            OrderEventDto event = new OrderEventDto(
                    order.getId(),
                    order.getOrderNumber(),
                    order.getCustomer().getId(),
                    order.getCustomer().getEmail(),
                    order.getStatus(),
                    order.getTotalAmount(),
                    LocalDateTime.now()
            );

            String routingKey = eventType.equals("ORDER_CREATED") ?
                    RabbitConfig.ORDER_CREATED_ROUTING_KEY : RabbitConfig.ORDER_STATUS_CHANGED_ROUTING_KEY;

            rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EXCHANGE, routingKey, event);
            logger.debug("Published order event: {} for order {}", eventType, order.getOrderNumber());
        } catch (Exception e) {
            logger.error("Failed to publish order event for order {}: {}", order.getOrderNumber(), e.getMessage());
        }
    }

    private void publishOrderStatusChangeEvent(Order order, String oldStatus, String newStatus) {
        publishOrderEvent(order, "ORDER_STATUS_CHANGED");
    }
}
