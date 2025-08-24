package crudjava.crudjava.dto;

import java.util.List;

public record CreateOrderRequestDTO(
    Long customerId,
    String shippingAddress,
    List<OrderItemRequestDTO> orderItems
) {
}
