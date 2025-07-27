package crudjava.crudjava.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_STATUS_CHANGED_QUEUE = "order.status.changed.queue";
    public static final String INVENTORY_UPDATE_QUEUE = "inventory.update.queue";
    public static final String LOW_STOCK_ALERT_QUEUE = "low.stock.alert.queue";
    public static final String EMAIL_NOTIFICATION_QUEUE = "email.notification.queue";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_STATUS_CHANGED_ROUTING_KEY = "order.status.changed";
    public static final String INVENTORY_UPDATE_ROUTING_KEY = "inventory.update";
    public static final String LOW_STOCK_ALERT_ROUTING_KEY = "low.stock.alert";
    public static final String EMAIL_NOTIFICATION_ROUTING_KEY = "email.notification";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(INVENTORY_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue orderStatusChangedQueue() {
        return QueueBuilder.durable(ORDER_STATUS_CHANGED_QUEUE).build();
    }

    @Bean
    public Queue inventoryUpdateQueue() {
        return QueueBuilder.durable(INVENTORY_UPDATE_QUEUE).build();
    }

    @Bean
    public Queue lowStockAlertQueue() {
        return QueueBuilder.durable(LOW_STOCK_ALERT_QUEUE).build();
    }

    @Bean
    public Queue emailNotificationQueue() {
        return QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(orderExchange())
                .with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding orderStatusChangedBinding() {
        return BindingBuilder.bind(orderStatusChangedQueue())
                .to(orderExchange())
                .with(ORDER_STATUS_CHANGED_ROUTING_KEY);
    }

    @Bean
    public Binding inventoryUpdateBinding() {
        return BindingBuilder.bind(inventoryUpdateQueue())
                .to(inventoryExchange())
                .with(INVENTORY_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Binding lowStockAlertBinding() {
        return BindingBuilder.bind(lowStockAlertQueue())
                .to(inventoryExchange())
                .with(LOW_STOCK_ALERT_ROUTING_KEY);
    }

    @Bean
    public Binding emailNotificationBinding() {
        return BindingBuilder.bind(emailNotificationQueue())
                .to(notificationExchange())
                .with(EMAIL_NOTIFICATION_ROUTING_KEY);
    }
}
