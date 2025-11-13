package crudjava.crudjava.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDTO {

    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
}
