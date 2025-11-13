package crudjava.crudjava.service;

import crudjava.crudjava.config.RabbitConfig;
import crudjava.crudjava.dto.CreateOrderRequestDTO;
import crudjava.crudjava.dto.OrderDTO;
import crudjava.crudjava.dto.OrderEventDto;
import crudjava.crudjava.dto.OrderItemRequestDTO;
import crudjava.crudjava.exception.CustomerNotFoundException;
import crudjava.crudjava.exception.InsufficientStockException;
import crudjava.crudjava.exception.OrderNotFoundException;
import crudjava.crudjava.exception.ProductNotFoundException;
import crudjava.crudjava.mapper.OrderMapper;
import crudjava.crudjava.model.Customer;
import crudjava.crudjava.model.Order;
import crudjava.crudjava.model.OrderItem;
import crudjava.crudjava.model.Product;
import crudjava.crudjava.repository.CustomerRepository;
import crudjava.crudjava.repository.OrderRepository;
import crudjava.crudjava.repository.ProductRepository;
import crudjava.crudjava.util.UrlUtils;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final RabbitTemplate rabbitTemplate;
    private final InventoryService inventoryService;

    @CircuitBreaker(
        name = "orderService",
        fallbackMethod = "fallbackCreateOrder"
    )
    public OrderDTO createOrder(CreateOrderRequestDTO request) {
        log.info(
            "Creating new order for customer ID: {}",
            request.getCustomerId()
        );

        Customer customer = customerRepository
            .findById(request.getCustomerId())
            .orElseThrow(() ->
                new CustomerNotFoundException(
                    "Customer not found: " + request.getCustomerId()
                )
            );

        String orderNumber =
            "ORDER-" + UUID.randomUUID().toString().substring(0, 8);

        Order order = Order.builder()
            .orderNumber(orderNumber)
            .customer(customer)
            .build();

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDTO itemRequest : request.getOrderItems()) {
            Product product = productRepository
                .findById(itemRequest.getProductId())
                .orElseThrow(() ->
                    new ProductNotFoundException(
                        "Product not found: " + itemRequest.getProductId()
                    )
                );

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName()
                );
            }

            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(itemRequest.getQuantity())
                .unitPrice(product.getPrice())
                .discountAmount(itemRequest.getDiscountAmount())
                .build();
            orderItem.calculateSubtotal();
            orderItems.add(orderItem);

            inventoryService.reserveInventory(
                product.getId(),
                itemRequest.getQuantity(),
                "Order: " + orderNumber
            );
        }

        order.setOrderItems(orderItems);
        order.calculateTotalAmount();

        try {
            Order savedOrder = orderRepository.save(order);
            log.info(
                "Successfully created order with ID: {}",
                savedOrder.getId()
            );

            OrderEventDto orderEvent = OrderEventDto.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .customerId(savedOrder.getCustomer().getId())
                .customerEmail(savedOrder.getCustomer().getEmail())
                .status("CREATED")
                .totalAmount(savedOrder.getTotalAmount())
                .eventTime(LocalDateTime.now())
                .build();
            rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EXCHANGE,
                RabbitConfig.ORDER_CREATED_ROUTING_KEY,
                orderEvent
            );

            return OrderMapper.toDTO(savedOrder);
        } catch (Exception ex) {
            log.error(
                "Order creation failed for customer {}: {}",
                request.getCustomerId(),
                ex.getMessage()
            );
            throw new RuntimeException("Failed to create order", ex);
        }
    }

    public OrderDTO createOrderFallback(
        CreateOrderRequestDTO request,
        Exception ex
    ) {
        log.error(
            "Order creation failed for customer {}: {}",
            request.getCustomerId(),
            ex.getMessage()
        );
        throw new RuntimeException(
            "Order service temporarily unavailable. Please try again later."
        );
    }

    @CircuitBreaker(name = "orderService")
    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository
            .findById(orderId)
            .orElseThrow(() ->
                new OrderNotFoundException("Order not found: " + orderId)
            );

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
                order
                    .getOrderItems()
                    .forEach(item ->
                        inventoryService.releaseInventory(
                            item.getProduct().getId(),
                            item.getQuantity(),
                            "Order cancelled: " + orderNumber
                        )
                    );
                break;
        }

        order = orderRepository.save(order);

        publishOrderStatusChangeEvent(order, oldStatus, newStatus);

        log.info(
            "Order {} status changed from {} to {}",
            order.getOrderNumber(),
            oldStatus,
            newStatus
        );
        return OrderMapper.toDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> findAll(Pageable pageable) {
        log.info("Finding all orders with pagination");
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(OrderMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<OrderDTO> findById(Long id) {
        return orderRepository
            .findById(id)
            .map(order -> {
                log.info("Found order with ID: {}", id);
                return OrderMapper.toDTO(order);
            });
    }

    @Transactional(readOnly = true)
    public Optional<OrderDTO> findByOrderNumber(String orderNumber) {
        String decodedOrderNumber = UrlUtils.autoDecodeIfNeeded(orderNumber);
        log.info("Searching order by number: {}", decodedOrderNumber);
        return orderRepository
            .findByOrderNumber(decodedOrderNumber)
            .map(order -> {
                log.info("Found order with number: {}", decodedOrderNumber);
                return OrderMapper.toDTO(order);
            });
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> findOrdersByCustomer(
        Long customerId,
        Pageable pageable
    ) {
        log.info("Finding orders for customer: {}", customerId);
        Page<Order> orders = orderRepository.findByCustomerIdAndStatus(
            customerId,
            null,
            pageable
        );
        return orders.map(OrderMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> findOrdersByDateRange(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        log.info("Finding orders between {} and {}", startDate, endDate);
        Page<Order> orders = orderRepository.findOrdersByDateRange(
            startDate,
            endDate,
            pageable
        );
        return orders.map(OrderMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> findHighValueOrders(BigDecimal minAmount) {
        log.info("Finding high value orders with min amount: {}", minAmount);
        List<String> statuses = List.of(
            "CONFIRMED",
            "PROCESSING",
            "SHIPPED",
            "DELIVERED"
        );
        List<Order> orders = orderRepository.findHighValueOrders(
            minAmount,
            statuses
        );
        return orders.stream().map(OrderMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getDailySalesReport(LocalDateTime startDate) {
        log.info("Generating daily sales report since: {}", startDate);
        return orderRepository.getDailySalesReport(startDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        log.info(
            "Calculating total revenue between {} and {}",
            startDate,
            endDate
        );
        BigDecimal revenue = orderRepository.getTotalRevenueByPeriod(
            startDate,
            endDate
        );
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    private void publishOrderEvent(Order order, String eventType) {
        try {
            OrderEventDto event = OrderEventDto.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomer().getId())
                .customerEmail(order.getCustomer().getEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .eventTime(LocalDateTime.now())
                .build();

            String routingKey = eventType.equals("ORDER_CREATED")
                ? RabbitConfig.ORDER_CREATED_ROUTING_KEY
                : RabbitConfig.ORDER_STATUS_CHANGED_ROUTING_KEY;

            rabbitTemplate.convertAndSend(
                RabbitConfig.ORDER_EXCHANGE,
                routingKey,
                event
            );
            log.debug(
                "Published order event: {} for order {}",
                eventType,
                order.getOrderNumber()
            );
        } catch (Exception e) {
            log.error(
                "Failed to publish order event for order {}: {}",
                order.getOrderNumber(),
                e.getMessage()
            );
        }
    }

    private void publishOrderStatusChangeEvent(
        Order order,
        String oldStatus,
        String newStatus
    ) {
        publishOrderEvent(order, "ORDER_STATUS_CHANGED");
    }
}
