package com.stockhub.alert.service;

import com.stockhub.alert.dto.AlertRequest;
import com.stockhub.alert.dto.AlertResponse;
import com.stockhub.alert.entity.Alert;
import com.stockhub.alert.enums.AlertSeverity;
import com.stockhub.alert.enums.AlertType;
import com.stockhub.alert.exception.AlertNotFoundException;
import com.stockhub.alert.repository.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AlertServiceImpl alertService;

    private Alert alert;
    private AlertRequest request;

    @BeforeEach
    void setUp() {
        // inject @Value field
        ReflectionTestUtils.setField(
                alertService,
                "adminEmail",
                "admin@stockhub.com");

        alert = Alert.builder()
                .alertId(1)
                .recipientId(1)
                .alertType(AlertType.LOW_STOCK)
                .severity(AlertSeverity.WARNING)
                .title("Low Stock: Laptop")
                .message("Stock is low")
                .relatedProductId(1)
                .relatedWarehouseId(1)
                .isRead(false)
                .isAcknowledged(false)
                .build();

        request = new AlertRequest();
        request.setRecipientId(1);
        request.setAlertType(AlertType.LOW_STOCK);
        request.setSeverity(AlertSeverity.WARNING);
        request.setTitle("Low Stock: Laptop");
        request.setMessage("Stock is low");
        request.setRelatedProductId(1);
        request.setRelatedWarehouseId(1);
    }

    // ─── Send Alert Tests ──────────────────────

    @Test
    void sendAlert_success() {
        when(alertRepository.save(any(Alert.class)))
                .thenReturn(alert);

        AlertResponse response =
                alertService.sendAlert(request);

        assertThat(response).isNotNull();
        assertThat(response.getAlertType())
                .isEqualTo(AlertType.LOW_STOCK);
        verify(alertRepository).save(any(Alert.class));
        // WARNING severity — no email
        verify(emailService, never())
                .sendEmail(anyString(), anyString(),
                        anyString());
    }

    @Test
    void sendAlert_criticalSeverity_sendsEmail() {
        request.setSeverity(AlertSeverity.CRITICAL);
        alert.setSeverity(AlertSeverity.CRITICAL);

        when(alertRepository.save(any(Alert.class)))
                .thenReturn(alert);

        alertService.sendAlert(request);

        // CRITICAL — email must be sent
        verify(emailService).sendEmail(
                anyString(), anyString(), anyString());
    }

    @Test
    void sendAlert_infoSeverity_noEmail() {
        request.setSeverity(AlertSeverity.INFO);
        alert.setSeverity(AlertSeverity.INFO);

        when(alertRepository.save(any(Alert.class)))
                .thenReturn(alert);

        alertService.sendAlert(request);

        verify(emailService, never())
                .sendEmail(anyString(), anyString(),
                        anyString());
    }

    // ─── Get Alert Tests ───────────────────────

    @Test
    void getAlertById_success() {
        when(alertRepository.findById(1))
                .thenReturn(Optional.of(alert));

        AlertResponse response =
                alertService.getAlertById(1);

        assertThat(response).isNotNull();
        assertThat(response.getAlertId()).isEqualTo(1);
    }

    @Test
    void getAlertById_notFound_throwsException() {
        when(alertRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                alertService.getAlertById(99))
                .isInstanceOf(
                        AlertNotFoundException.class);
    }

    @Test
    void getAlertsByRecipient_success() {
        when(alertRepository.findByRecipientId(1))
                .thenReturn(List.of(alert));

        List<AlertResponse> result =
                alertService.getAlertsByRecipient(1);

        assertThat(result).hasSize(1);
    }

    @Test
    void getUnreadAlerts_success() {
        when(alertRepository
                .findByRecipientIdAndIsReadFalse(1))
                .thenReturn(List.of(alert));

        List<AlertResponse> result =
                alertService.getUnreadAlerts(1);

        assertThat(result).hasSize(1);
    }

    @Test
    void getUnreadCount_success() {
        when(alertRepository
                .countByRecipientIdAndIsReadFalse(1))
                .thenReturn(5L);

        long count = alertService.getUnreadCount(1);

        assertThat(count).isEqualTo(5L);
    }

    // ─── Mark / Acknowledge Tests ──────────────

    @Test
    void markAsRead_success() {
        when(alertRepository.findById(1))
                .thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class)))
                .thenReturn(alert);

        alertService.markAsRead(1);

        assertThat(alert.isRead()).isTrue();
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void markAllAsRead_success() {
        when(alertRepository
                .findByRecipientIdAndIsReadFalse(1))
                .thenReturn(List.of(alert));
        when(alertRepository.saveAll(anyList()))
                .thenReturn(List.of(alert));

        alertService.markAllAsRead(1);

        assertThat(alert.isRead()).isTrue();
        verify(alertRepository).saveAll(anyList());
    }

    @Test
    void acknowledgeAlert_success() {
        when(alertRepository.findById(1))
                .thenReturn(Optional.of(alert));
        when(alertRepository.save(any(Alert.class)))
                .thenReturn(alert);

        alertService.acknowledgeAlert(1);

        assertThat(alert.isRead()).isTrue();
        assertThat(alert.isAcknowledged()).isTrue();
    }

    @Test
    void deleteAlert_success() {
        when(alertRepository.findById(1))
                .thenReturn(Optional.of(alert));

        alertService.deleteAlert(1);

        verify(alertRepository).deleteById(1);
    }

    @Test
    void deleteAlert_notFound_throwsException() {
        when(alertRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                alertService.deleteAlert(99))
                .isInstanceOf(
                        AlertNotFoundException.class);
    }

    @Test
    void getAllAlerts_success() {
        when(alertRepository.findAll())
                .thenReturn(List.of(alert));

        List<AlertResponse> result =
                alertService.getAllAlerts();

        assertThat(result).hasSize(1);
    }
}