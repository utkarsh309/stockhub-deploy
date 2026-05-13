package com.stockhub.alert.messaging;

import com.stockhub.alert.config.RabbitMQConfig;
import com.stockhub.alert.dto.AlertRequest;
import com.stockhub.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertConsumer {

    private final AlertService alertService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consume(AlertRequest alertRequest) {

        try {
            log.info("Alert received from RabbitMQ: " +
                            "type={} severity={} recipient={}",
                    alertRequest.getAlertType(),
                    alertRequest.getSeverity(),
                    alertRequest.getRecipientId());

            // single call handles everything
            // 1. saves alert entity to DB
            // 2. sends email for CRITICAL severity
            //    via emailService.sendEmail() internally
            // zero changes needed in AlertServiceImpl
            alertService.sendAlert(alertRequest);

            log.info("Alert processed successfully: " +
                            "type={} recipient={}",
                    alertRequest.getAlertType(),
                    alertRequest.getRecipientId());

        } catch (Exception e) {
            log.error("Failed to process alert " +
                    "from RabbitMQ: {}", e.getMessage());
        }
    }
}