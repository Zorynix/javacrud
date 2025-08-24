package crudjava.crudjava.dto;

import java.time.LocalDateTime;

public record InventoryEventDto(
    Long productId,
    String productName,
    String sku,
    Integer oldQuantity,
    Integer newQuantity,
    String operation, // DECREASE, INCREASE, SET
    String reason,
    LocalDateTime eventTime
) {
}
