package crudjava.crudjava.listener;

import crudjava.crudjava.config.RabbitConfig;
import crudjava.crudjava.dto.EmailNotificationDto;
import crudjava.crudjava.dto.InventoryEventDto;
import crudjava.crudjava.dto.ProcurementAlertDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.INVENTORY_UPDATE_QUEUE)
    public void handleInventoryUpdate(InventoryEventDto inventoryEvent) {
        log.info(
            "Processing inventory update event: Product {} {} from {} to {}",
            inventoryEvent.getSku(),
            inventoryEvent.getOperation(),
            inventoryEvent.getOldQuantity(),
            inventoryEvent.getNewQuantity()
        );

        try {
            log.info(
                "INVENTORY_CHANGE: Product={}, SKU={}, Operation={}, Old={}, New={}, Reason={}",
                inventoryEvent.getProductName(),
                inventoryEvent.getSku(),
                inventoryEvent.getOperation(),
                inventoryEvent.getOldQuantity(),
                inventoryEvent.getNewQuantity(),
                inventoryEvent.getReason()
            );
        } catch (Exception e) {
            log.error(
                "Failed to process inventory update event for product {}: {}",
                inventoryEvent.getSku(),
                e.getMessage()
            );
        }
    }

    @RabbitListener(queues = RabbitConfig.LOW_STOCK_ALERT_QUEUE)
    public void handleLowStockAlert(InventoryEventDto inventoryEvent) {
        log.warn(
            "Processing low stock alert: Product {} has only {} units remaining",
            inventoryEvent.getSku(),
            inventoryEvent.getNewQuantity()
        );

        try {
            ProcurementAlertDto procurementAlert = ProcurementAlertDto.builder()
                .productId(inventoryEvent.getProductId())
                .productName(inventoryEvent.getProductName())
                .sku(inventoryEvent.getSku())
                .currentStock(inventoryEvent.getNewQuantity())
                .alertType("LOW_STOCK_ALERT")
                .message(
                    "Urgent: Product " +
                        inventoryEvent.getProductName() +
                        " (" +
                        inventoryEvent.getSku() +
                        ") is running low. Only " +
                        inventoryEvent.getNewQuantity() +
                        " units remaining."
                )
                .build();

            log.info(
                "Low stock alert generated for product: {} ({})",
                inventoryEvent.getProductName(),
                inventoryEvent.getSku()
            );

            if (inventoryEvent.getNewQuantity() <= 5) {
                log.warn(
                    "CRITICAL: Product {} has reached critical stock level!",
                    inventoryEvent.getSku()
                );
            }
        } catch (Exception e) {
            log.error(
                "Failed to process low stock alert for product {}: {}",
                inventoryEvent.getSku(),
                e.getMessage()
            );
        }
    }

    @RabbitListener(queues = RabbitConfig.EMAIL_NOTIFICATION_QUEUE)
    public void handleEmailNotification(
        EmailNotificationDto emailNotification
    ) {
        log.info(
            "Processing email notification: {} to {}",
            emailNotification.getSubject(),
            emailNotification.getRecipientEmail()
        );

        try {
            log.info(
                "EMAIL SENT: To={}, Subject={}, Type={}",
                emailNotification.getRecipientEmail(),
                emailNotification.getSubject(),
                emailNotification.getEventType()
            );

            Thread.sleep(100);

            log.info("Email notification processed successfully");
        } catch (Exception e) {
            log.error(
                "Failed to send email notification to {}: {}",
                emailNotification.getRecipientEmail(),
                e.getMessage()
            );
        }
    }
}
