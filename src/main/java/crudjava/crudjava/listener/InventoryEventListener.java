package crudjava.crudjava.listener;

import crudjava.crudjava.config.RabbitConfig;
import crudjava.crudjava.dto.InventoryEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventListener {

    private static final Logger logger = LoggerFactory.getLogger(InventoryEventListener.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.INVENTORY_UPDATE_QUEUE)
    public void handleInventoryUpdate(InventoryEventDto inventoryEvent) {
        logger.info("Processing inventory update event: Product {} {} from {} to {}",
                inventoryEvent.getSku(), inventoryEvent.getOperation(),
                inventoryEvent.getOldQuantity(), inventoryEvent.getNewQuantity());

        try {
            logger.info("INVENTORY_CHANGE: Product={}, SKU={}, Operation={}, Old={}, New={}, Reason={}",
                    inventoryEvent.getProductName(), inventoryEvent.getSku(),
                    inventoryEvent.getOperation(), inventoryEvent.getOldQuantity(),
                    inventoryEvent.getNewQuantity(), inventoryEvent.getReason());


        } catch (Exception e) {
            logger.error("Failed to process inventory update event for product {}: {}",
                    inventoryEvent.getSku(), e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitConfig.LOW_STOCK_ALERT_QUEUE)
    public void handleLowStockAlert(InventoryEventDto inventoryEvent) {
        logger.warn("Processing low stock alert: Product {} has only {} units remaining",
                inventoryEvent.getSku(), inventoryEvent.getNewQuantity());

        try {
            ProcurementAlertDto procurementAlert = new ProcurementAlertDto(
                inventoryEvent.getProductId(),
                inventoryEvent.getProductName(),
                inventoryEvent.getSku(),
                inventoryEvent.getNewQuantity(),
                "LOW_STOCK_ALERT",
                "Urgent: Product " + inventoryEvent.getProductName() +
                " (" + inventoryEvent.getSku() + ") is running low. Only " +
                inventoryEvent.getNewQuantity() + " units remaining."
            );

            logger.info("Low stock alert generated for product: {} ({})",
                    inventoryEvent.getProductName(), inventoryEvent.getSku());

            if (inventoryEvent.getNewQuantity() <= 5) {
                logger.warn("CRITICAL: Product {} has reached critical stock level!",
                        inventoryEvent.getSku());
            }

        } catch (Exception e) {
            logger.error("Failed to process low stock alert for product {}: {}",
                    inventoryEvent.getSku(), e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitConfig.EMAIL_NOTIFICATION_QUEUE)
    public void handleEmailNotification(OrderEventListener.EmailNotificationDto emailNotification) {
        logger.info("Processing email notification: {} to {}",
                emailNotification.getSubject(), emailNotification.getRecipientEmail());

        try {
            logger.info("EMAIL SENT: To={}, Subject={}, Type={}",
                    emailNotification.getRecipientEmail(),
                    emailNotification.getSubject(),
                    emailNotification.getEventType());

            Thread.sleep(100);

            logger.info("Email notification processed successfully");

        } catch (Exception e) {
            logger.error("Failed to send email notification to {}: {}",
                    emailNotification.getRecipientEmail(), e.getMessage());

        }
    }

    public static class ProcurementAlertDto {
        private Long productId;
        private String productName;
        private String sku;
        private Integer currentStock;
        private String alertType;
        private String message;

        public ProcurementAlertDto() {}

        public ProcurementAlertDto(Long productId, String productName, String sku,
                                 Integer currentStock, String alertType, String message) {
            this.productId = productId;
            this.productName = productName;
            this.sku = sku;
            this.currentStock = currentStock;
            this.alertType = alertType;
            this.message = message;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }

        public Integer getCurrentStock() { return currentStock; }
        public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
