package crudjava.crudjava.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEventDto {

    private Long orderId;
    private String orderNumber;
    private Long customerId;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime eventTime;
}
