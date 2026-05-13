package com.stockhub.alert.messaging;

import com.stockhub.alert.config.RabbitMQConfig;
import com.stockhub.alert.dto.AlertRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(AlertRequest alertRequest) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                alertRequest
        );

        log.info("Alert published to RabbitMQ: " +
                        "type={} severity={} recipient={}",
                alertRequest.getAlertType(),
                alertRequest.getSeverity(),
                alertRequest.getRecipientId());
    }
}