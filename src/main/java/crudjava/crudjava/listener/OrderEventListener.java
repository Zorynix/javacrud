package crudjava.crudjava.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import crudjava.crudjava.config.RabbitConfig;
import crudjava.crudjava.dto.OrderEventDto;

@Component
public class OrderEventListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderEventDto orderEvent) {
        logger.info("Processing order created event: Order {} for customer {}",
                orderEvent.orderNumber(), orderEvent.customerEmail());

        try {
            EmailNotificationDto emailNotification = new EmailNotificationDto(
                orderEvent.customerEmail(),
                "Order Confirmation - " + orderEvent.orderNumber(),
                "Your order " + orderEvent.orderNumber() + " has been created successfully. " +
                "Total amount: $" + orderEvent.totalAmount(),
                "ORDER_CREATED"
            );

            rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_EXCHANGE,
                    RabbitConfig.EMAIL_NOTIFICATION_ROUTING_KEY, emailNotification);

            logger.info("Email notification sent for order: {}", orderEvent.orderNumber());
        } catch (Exception e) {
            logger.error("Failed to process order created event for order {}: {}",
                    orderEvent.orderNumber(), e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitConfig.ORDER_STATUS_CHANGED_QUEUE)
    public void handleOrderStatusChanged(OrderEventDto orderEvent) {
        logger.info("Processing order status change event: Order {} status changed to {}",
                orderEvent.orderNumber(), orderEvent.status());

        try {
            String subject = "Order Update - " + orderEvent.orderNumber();
            String message = "Your order " + orderEvent.orderNumber() +
                    " status has been updated to: " + orderEvent.status();

            switch (orderEvent.status()) {
                case "SHIPPED":
                    message += ". Your order is on its way!";
                    break;
                case "DELIVERED":
                    message += ". Thank you for your business!";
                    break;
                case "CANCELLED":
                    message += ". If you have any questions, please contact support.";
                    break;
            }

            EmailNotificationDto emailNotification = new EmailNotificationDto(
                orderEvent.customerEmail(),
                subject,
                message,
                "ORDER_STATUS_CHANGED"
            );

            rabbitTemplate.convertAndSend(RabbitConfig.NOTIFICATION_EXCHANGE,
                    RabbitConfig.EMAIL_NOTIFICATION_ROUTING_KEY, emailNotification);

            logger.info("Status change notification sent for order: {}", orderEvent.orderNumber());
        } catch (Exception e) {
            logger.error("Failed to process order status change event for order {}: {}",
                    orderEvent.orderNumber(), e.getMessage());
        }
    }

    public static class EmailNotificationDto {
        private String recipientEmail;
        private String subject;
        private String message;
        private String eventType;

        public EmailNotificationDto() {}

        public EmailNotificationDto(String recipientEmail, String subject, String message, String eventType) {
            this.recipientEmail = recipientEmail;
            this.subject = subject;
            this.message = message;
            this.eventType = eventType;
        }

        public String getRecipientEmail() { return recipientEmail; }
        public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
}
