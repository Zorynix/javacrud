package crudjava.crudjava.listener;

import crudjava.crudjava.config.RabbitConfig;
import crudjava.crudjava.dto.EmailNotificationDto;
import crudjava.crudjava.dto.OrderEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void handleOrderCreated(OrderEventDto orderEvent) {
        log.info(
            "Processing order created event: Order {} for customer {}",
            orderEvent.getOrderNumber(),
            orderEvent.getCustomerEmail()
        );

        try {
            EmailNotificationDto emailNotification =
                EmailNotificationDto.builder()
                    .recipientEmail(orderEvent.getCustomerEmail())
                    .subject(
                        "Order Confirmation - " + orderEvent.getOrderNumber()
                    )
                    .message(
                        "Your order " +
                            orderEvent.getOrderNumber() +
                            " has been created successfully. " +
                            "Total amount: $" +
                            orderEvent.getTotalAmount()
                    )
                    .eventType("ORDER_CREATED")
                    .build();

            rabbitTemplate.convertAndSend(
                RabbitConfig.NOTIFICATION_EXCHANGE,
                RabbitConfig.EMAIL_NOTIFICATION_ROUTING_KEY,
                emailNotification
            );

            log.info(
                "Email notification sent for order: {}",
                orderEvent.getOrderNumber()
            );
        } catch (Exception e) {
            log.error(
                "Failed to process order created event for order {}: {}",
                orderEvent.getOrderNumber(),
                e.getMessage()
            );
        }
    }

    @RabbitListener(queues = RabbitConfig.ORDER_STATUS_CHANGED_QUEUE)
    public void handleOrderStatusChanged(OrderEventDto orderEvent) {
        log.info(
            "Processing order status change event: Order {} status changed to {}",
            orderEvent.getOrderNumber(),
            orderEvent.getStatus()
        );

        try {
            String subject = "Order Update - " + orderEvent.getOrderNumber();
            String message =
                "Your order " +
                orderEvent.getOrderNumber() +
                " status has been updated to: " +
                orderEvent.getStatus();

            switch (orderEvent.getStatus()) {
                case "SHIPPED":
                    message += ". Your order is on its way!";
                    break;
                case "DELIVERED":
                    message += ". Thank you for your business!";
                    break;
                case "CANCELLED":
                    message +=
                        ". If you have any questions, please contact support.";
                    break;
            }

            EmailNotificationDto emailNotification =
                EmailNotificationDto.builder()
                    .recipientEmail(orderEvent.getCustomerEmail())
                    .subject(subject)
                    .message(message)
                    .eventType("ORDER_STATUS_CHANGED")
                    .build();

            rabbitTemplate.convertAndSend(
                RabbitConfig.NOTIFICATION_EXCHANGE,
                RabbitConfig.EMAIL_NOTIFICATION_ROUTING_KEY,
                emailNotification
            );

            log.info(
                "Status change notification sent for order: {}",
                orderEvent.getOrderNumber()
            );
        } catch (Exception e) {
            log.error(
                "Failed to process order status change event for order {}: {}",
                orderEvent.getOrderNumber(),
                e.getMessage()
            );
        }
    }
}
