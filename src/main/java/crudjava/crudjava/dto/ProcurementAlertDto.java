package crudjava.crudjava.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementAlertDto {

    private Long productId;
    private String productName;
    private String sku;
    private Integer currentStock;
    private String alertType;
    private String message;
}
