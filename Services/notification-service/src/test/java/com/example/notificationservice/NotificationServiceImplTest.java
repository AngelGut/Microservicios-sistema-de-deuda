package com.example.notificationservice;

import com.example.notificationservice.dto.request.PaymentConfirmationRequest;
import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.entity.NotificationLog;
import com.example.notificationservice.repository.NotificationLogRepository;
import com.example.notificationservice.service.EmailService;
import com.example.notificationservice.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para {@link NotificationServiceImpl}.
 *
 * <p>Se validan los flujos de éxito y de fallo en el procesamiento
 * de una confirmación de pago, verificando que:
 * <ul>
 *   <li>Se delega correctamente al EmailService.</li>
 *   <li>Se persiste el log con el estado correcto.</li>
 *   <li>La respuesta contiene los datos esperados.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private EmailService emailService;

    @Mock
    private NotificationLogRepository logRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private PaymentConfirmationRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleRequest = new PaymentConfirmationRequest(
                "debt-uuid-001",
                "42",
                "Juan Pérez",
                "juan.perez@email.com",
                new BigDecimal("150.00"),
                new BigDecimal("350.00"),
                "USD",
                LocalDate.of(2025, 1, 15)
        );
    }

    @Test
    @DisplayName("Debe enviar email y persistir log con estado SENT cuando el envío es exitoso")
    void processPaymentConfirmation_success_shouldPersistSentLog() {
        // Arrange
        doNothing().when(emailService).sendPaymentConfirmation(any());

        NotificationLog savedLog = NotificationLog.builder()
                .id(1L)
                .type(NotificationLog.NotificationType.PAYMENT_CONFIRMATION)
                .recipientEmail("juan.perez@email.com")
                .status(NotificationLog.NotificationStatus.SENT)
                .message("Correo de confirmación enviado correctamente")
                .build();

        when(logRepository.save(any(NotificationLog.class))).thenReturn(savedLog);

        // Act
        NotificationResponse response = notificationService.processPaymentConfirmation(sampleRequest);

        // Assert
        verify(emailService, times(1)).sendPaymentConfirmation(sampleRequest);

        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository, times(1)).save(logCaptor.capture());

        NotificationLog captured = logCaptor.getValue();
        assertThat(captured.getStatus()).isEqualTo(NotificationLog.NotificationStatus.SENT);
        assertThat(captured.getType()).isEqualTo(NotificationLog.NotificationType.PAYMENT_CONFIRMATION);
        assertThat(captured.getReferenceId()).isEqualTo("debt-uuid-001");
        assertThat(captured.getRecipientEmail()).isEqualTo("juan.perez@email.com");

        assertThat(response.status()).isEqualTo("SENT");
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Debe persistir log con estado FAILED cuando el envío de email falla")
    void processPaymentConfirmation_emailFails_shouldPersistFailedLog() {
        // Arrange
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendPaymentConfirmation(any());

        NotificationLog failedLog = NotificationLog.builder()
                .id(2L)
                .type(NotificationLog.NotificationType.PAYMENT_CONFIRMATION)
                .recipientEmail("juan.perez@email.com")
                .status(NotificationLog.NotificationStatus.FAILED)
                .message("Error al enviar correo: SMTP error")
                .build();

        when(logRepository.save(any(NotificationLog.class))).thenReturn(failedLog);

        // Act
        NotificationResponse response = notificationService.processPaymentConfirmation(sampleRequest);

        // Assert
        ArgumentCaptor<NotificationLog> logCaptor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(logRepository, times(1)).save(logCaptor.capture());

        NotificationLog captured = logCaptor.getValue();
        assertThat(captured.getStatus()).isEqualTo(NotificationLog.NotificationStatus.FAILED);
        assertThat(captured.getMessage()).contains("SMTP error");

        assertThat(response.status()).isEqualTo("FAILED");
    }
}
