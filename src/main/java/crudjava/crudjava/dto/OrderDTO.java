package crudjava.crudjava.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import crudjava.crudjava.model.Order;
import crudjava.crudjava.model.OrderItem;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> items;

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.customerId = order.getCustomer().getId();
        this.customerName = order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.orderDate = order.getCreatedAt();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        
        if (order.getOrderItems() != null) {
            this.items = order.getOrderItems().stream()
                .map(OrderItemDTO::new)
                .toList();
        }
    }

    @Data
    @NoArgsConstructor
    public static class OrderItemDTO {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;

        public OrderItemDTO(OrderItem item) {
            this.id = item.getId();
            this.productId = item.getProduct().getId();
            this.productName = item.getProduct().getName();
            this.productSku = item.getProduct().getSku();
            this.quantity = item.getQuantity();
            this.unitPrice = item.getUnitPrice();
            this.totalPrice = item.getTotalPrice();
        }
    }
}
