package crudjava.crudjava.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import crudjava.crudjava.model.Order;
import crudjava.crudjava.model.OrderItem;

public record OrderDTO(
    Long id,
    String orderNumber,
    Long customerId,
    String customerName,
    BigDecimal totalAmount,
    String status,
    LocalDateTime orderDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<OrderItemDTO> items
) {
    public OrderDTO(Order order) {
        this(
            order.getId(),
            order.getOrderNumber(),
            order.getCustomer().getId(),
            order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getOrderItems() != null
                ? order.getOrderItems().stream().map(OrderItemDTO::new).toList()
                : null
        );
    }

    public record OrderItemDTO(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
    ) {
        public OrderItemDTO(OrderItem item) {
            this(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
            );
        }
    }
}
