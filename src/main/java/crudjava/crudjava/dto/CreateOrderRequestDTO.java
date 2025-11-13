package crudjava.crudjava.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequestDTO {

    private Long customerId;
    private String shippingAddress;
    private List<OrderItemRequestDTO> orderItems;
}
