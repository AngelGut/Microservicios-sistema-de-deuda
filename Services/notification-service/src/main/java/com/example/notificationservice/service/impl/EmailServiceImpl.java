package com.example.notificationservice.service.impl;

import com.example.notificationservice.dto.request.PaymentConfirmationRequest;
import com.example.notificationservice.dto.response.DebtDto;
import com.example.notificationservice.dto.response.DebtorDto;
import com.example.notificationservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

/**
 * Implementación del servicio de envío de correos electrónicos.
 *
 * <p>Principios SOLID aplicados:
 * <ul>
 *   <li><b>SRP</b>: solo construye y envía correos; no persiste logs ni orquesta flujos.</li>
 *   <li><b>OCP</b>: añadir un nuevo tipo de email implica solo crear una plantilla
 *       y un método sin modificar los existentes.</li>
 *   <li><b>LSP</b>: cumple íntegramente el contrato de {@link EmailService}.</li>
 *   <li><b>DIP</b>: depende de abstracciones ({@link JavaMailSender}, {@link TemplateEngine}).</li>
 * </ul>
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String TEMPLATE_PAYMENT_CONFIRMATION = "payment-confirmation";
    private static final String TEMPLATE_PAYMENT_REMINDER     = "payment-reminder";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender     = mailSender;
        this.templateEngine = templateEngine;
    }

    // ── Confirmación de pago ─────────────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Construye un correo HTML usando la plantilla
     * {@code payment-confirmation.html} de Thymeleaf y lo envía
     * al email del deudor contenido en {@code request}.
     */
    @Override
    public void sendPaymentConfirmation(PaymentConfirmationRequest request) {
        log.info("Enviando confirmación de pago a {} para deuda {}",
                request.debtorEmail(), request.debtId());

        Context ctx = new Context();
        ctx.setVariable("debtorName",       request.debtorName());
        ctx.setVariable("amountPaid",       request.amountPaid());
        ctx.setVariable("remainingBalance", request.remainingBalance());
        ctx.setVariable("currency",         request.currency());
        ctx.setVariable("paymentDate",      request.paymentDate().format(DATE_FORMAT));
        ctx.setVariable("debtId",           request.debtId());

        String html = templateEngine.process(TEMPLATE_PAYMENT_CONFIRMATION, ctx);

        sendHtmlEmail(
                request.debtorEmail(),
                "Confirmación de pago - Referencia #" + request.debtId(),
                html
        );

        log.info("Confirmación de pago enviada exitosamente a {}", request.debtorEmail());
    }

    // ── Recordatorio de vencimiento ──────────────────────────

    /**
     * {@inheritDoc}
     *
     * <p>Construye un correo HTML usando la plantilla
     * {@code payment-reminder.html} de Thymeleaf y lo envía
     * al email del deudor.
     */
    @Override
    public void sendPaymentReminder(DebtDto debt, DebtorDto debtor) {
        log.info("Enviando recordatorio de vencimiento a {} para deuda {}",
                debtor.email(), debt.id());

        Context ctx = new Context();
        ctx.setVariable("debtorName",   debtor.name());
        ctx.setVariable("debtAmount",   debt.currentBalance());
        ctx.setVariable("currency",     debt.currency());
        ctx.setVariable("dueDate",      debt.dueDate().format(DATE_FORMAT));
        ctx.setVariable("description",  debt.description());
        ctx.setVariable("debtId",       debt.id());

        String html = templateEngine.process(TEMPLATE_PAYMENT_REMINDER, ctx);

        sendHtmlEmail(
                debtor.email(),
                "Recordatorio: Tu deuda vence en 3 días - Ref #" + debt.id(),
                html
        );

        log.info("Recordatorio de vencimiento enviado a {}", debtor.email());
    }

    // ── Método privado de envío ──────────────────────────────

    /**
     * Construye y envía un correo HTML usando {@link JavaMailSender}.
     *
     * @param to      dirección de correo del destinatario
     * @param subject asunto del correo
     * @param html    contenido HTML del cuerpo del mensaje
     * @throws RuntimeException si ocurre un error de mensajería
     */
    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = isHtml

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Error al enviar correo a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Fallo al enviar correo a " + to, e);
        }
    }
}
