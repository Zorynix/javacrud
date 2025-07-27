package crudjava.crudjava.service;

import crudjava.crudjava.config.RabbitConfig;
import crudjava.crudjava.dto.InventoryEventDto;
import crudjava.crudjava.dto.OrderEventDto;
import crudjava.crudjava.model.*;
import crudjava.crudjava.repository.CustomerRepository;
import crudjava.crudjava.repository.OrderRepository;
import crudjava.crudjava.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private InventoryService inventoryService;

    @CircuitBreaker(name = "orderService", fallbackMethod = "createOrderFallback")
    public Order createOrder(Long customerId, List<OrderItemRequest> items) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order(orderNumber, customer);
        order = orderRepository.save(order);

        for (OrderItemRequest itemRequest : items) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem(order, product, itemRequest.getQuantity(), product.getPrice());
            if (itemRequest.getDiscountAmount() != null) {
                orderItem.setDiscountAmount(itemRequest.getDiscountAmount());
            }

            inventoryService.reserveInventory(product.getId(), itemRequest.getQuantity(),
                    "Order reservation: " + orderNumber);
        }

        order = orderRepository.findById(order.getId()).orElseThrow();
        order.calculateTotalAmount();
        order = orderRepository.save(order);

        publishOrderEvent(order, "ORDER_CREATED");

        logger.info("Order created successfully: {}", orderNumber);
        return order;
    }

    public Order createOrderFallback(Long customerId, List<OrderItemRequest> items, Exception ex) {
        logger.error("Order creation failed for customer {}: {}", customerId, ex.getMessage());
        throw new RuntimeException("Order service temporarily unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "orderService")
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        switch (newStatus) {
            case SHIPPED:
                order.setShippedAt(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case CANCELLED:
                final String orderNumber = order.getOrderNumber();
                order.getOrderItems().forEach(item ->
                    inventoryService.releaseInventory(item.getProduct().getId(),
                            item.getQuantity(), "Order cancelled: " + orderNumber));
                break;
        }

        order = orderRepository.save(order);

        publishOrderStatusChangeEvent(order, oldStatus, newStatus);

        logger.info("Order {} status changed from {} to {}", order.getOrderNumber(), oldStatus, newStatus);
        return order;
    }

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional(readOnly = true)
    public Page<Order> findOrdersByCustomer(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdAndStatus(customerId, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findOrdersByDateRange(startDate, endDate, pageable);
    }

    @Transactional(readOnly = true)
    public List<Order> findHighValueOrders(BigDecimal minAmount) {
        List<Order.OrderStatus> statuses = List.of(Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PROCESSING, Order.OrderStatus.SHIPPED, Order.OrderStatus.DELIVERED);
        return orderRepository.findHighValueOrders(minAmount, statuses);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getDailySalesReport(LocalDateTime startDate) {
        return orderRepository.getDailySalesReport(startDate);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
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
                    order.getStatus().name(),
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

    private void publishOrderStatusChangeEvent(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus) {
        publishOrderEvent(order, "ORDER_STATUS_CHANGED");
    }

    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        private BigDecimal discountAmount;

        public OrderItemRequest() {}

        public OrderItemRequest(Long productId, Integer quantity, BigDecimal discountAmount) {
            this.productId = productId;
            this.quantity = quantity;
            this.discountAmount = discountAmount;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    }
}
