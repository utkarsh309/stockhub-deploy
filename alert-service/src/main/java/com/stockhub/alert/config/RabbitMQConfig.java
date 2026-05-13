package com.stockhub.alert.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE   = "stockhub.alerts";
    public static final String QUEUE      = "alert.notification.queue";
    public static final String ROUTING_KEY = "alert.notification";

    //  Queue
    // durable=true means queue survives RabbitMQ restart
    @Bean
    public Queue alertQueue() {
        return new Queue(QUEUE, true);
    }

    // Exchange
    @Bean
    public DirectExchange alertExchange() {
        return new DirectExchange(EXCHANGE);
    }

    //  Binding
    // Binds queue to exchange with routing key
    @Bean
    public Binding alertBinding(Queue alertQueue,
                                DirectExchange alertExchange) {
        return BindingBuilder
                .bind(alertQueue)
                .to(alertExchange)
                .with(ROUTING_KEY);
    }

    // JSON Message Converter
    // Serialize AlertRequest as JSON in the queue
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Rabbit Template
    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory) {
        RabbitTemplate template =
                new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}