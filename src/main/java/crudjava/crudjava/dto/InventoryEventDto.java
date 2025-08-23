package crudjava.crudjava.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEventDto {
    private Long productId;
    private String productName;
    private String sku;
    private Integer oldQuantity;
    private Integer newQuantity;
    private String operation; // DECREASE, INCREASE, SET
    private String reason;
    private LocalDateTime eventTime;
}
