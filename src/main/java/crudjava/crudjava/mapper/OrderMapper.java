package crudjava.crudjava.mapper;

import crudjava.crudjava.dto.OrderDTO;
import crudjava.crudjava.model.Order;
import crudjava.crudjava.model.OrderItem;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderMapper {

    public static OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }

        return OrderDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .customerId(order.getCustomer().getId())
            .customerName(
                order.getCustomer().getFirstName() +
                    " " +
                    order.getCustomer().getLastName()
            )
            .totalAmount(order.getTotalAmount())
            .status(order.getStatus())
            .orderDate(order.getCreatedAt())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .items(
                order.getOrderItems() != null
                    ? order.getOrderItems()
                        .stream()
                        .map(OrderMapper::toOrderItemDTO)
                        .collect(Collectors.toList())
                    : null
            )
            .build();
    }

    public static OrderDTO.OrderItemDTO toOrderItemDTO(OrderItem item) {
        if (item == null) {
            return null;
        }

        return OrderDTO.OrderItemDTO.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .productSku(item.getProduct().getSku())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .totalPrice(item.getTotalPrice())
            .build();
    }
}
