package crudjava.crudjava.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderEventDto(
    Long orderId,
    String orderNumber,
    Long customerId,
    String customerEmail,
    String status,
    BigDecimal totalAmount,
    LocalDateTime eventTime
) {
}
