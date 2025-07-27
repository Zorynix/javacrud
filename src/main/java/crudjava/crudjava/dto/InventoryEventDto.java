package crudjava.crudjava.dto;

import java.time.LocalDateTime;

public class InventoryEventDto {
    private Long productId;
    private String productName;
    private String sku;
    private Integer oldQuantity;
    private Integer newQuantity;
    private String operation; // DECREASE, INCREASE, SET
    private String reason;
    private LocalDateTime eventTime;

    public InventoryEventDto() {}

    public InventoryEventDto(Long productId, String productName, String sku,
                           Integer oldQuantity, Integer newQuantity,
                           String operation, String reason, LocalDateTime eventTime) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.oldQuantity = oldQuantity;
        this.newQuantity = newQuantity;
        this.operation = operation;
        this.reason = reason;
        this.eventTime = eventTime;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Integer getOldQuantity() { return oldQuantity; }
    public void setOldQuantity(Integer oldQuantity) { this.oldQuantity = oldQuantity; }

    public Integer getNewQuantity() { return newQuantity; }
    public void setNewQuantity(Integer newQuantity) { this.newQuantity = newQuantity; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
