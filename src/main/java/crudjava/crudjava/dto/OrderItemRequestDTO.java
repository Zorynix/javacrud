package crudjava.crudjava.dto;

import java.math.BigDecimal;

public record OrderItemRequestDTO(
    Long productId,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal discountAmount
) {
}
