package com.example.notificationservice.service.impl;

import com.example.notificationservice.dto.request.PaymentConfirmationRequest;
import com.example.notificationservice.dto.response.DebtDto;
import com.example.notificationservice.dto.response.DebtorDto;
import com.example.notificationservice.service.EmailService;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@Primary
public class SendGridEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SendGrid sendGrid;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    public SendGridEmailService(
            @Value("${sendgrid.api-key}") String apiKey,
            TemplateEngine templateEngine) {
        this.sendGrid = new SendGrid(apiKey);
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendPaymentConfirmation(PaymentConfirmationRequest request) {
        log.info("Enviando confirmación de pago a {}", request.debtorEmail());

        Context ctx = new Context();
        ctx.setVariable("debtorName",       request.debtorName());
        ctx.setVariable("amountPaid",       request.amountPaid());
        ctx.setVariable("remainingBalance", request.remainingBalance());
        ctx.setVariable("currency",         request.currency());
        ctx.setVariable("paymentDate",      request.paymentDate().format(DATE_FORMAT));
        ctx.setVariable("debtId",           request.debtId());

        String html = templateEngine.process("payment-confirmation", ctx);
        sendEmail(request.debtorEmail(), "Confirmación de pago - Ref #" + request.debtId(), html);
    }

    @Override
    public void sendPaymentReminder(DebtDto debt, DebtorDto debtor) {
        log.info("Enviando recordatorio a {}", debtor.email());

        Context ctx = new Context();
        ctx.setVariable("debtorName",  debtor.name());
        ctx.setVariable("debtAmount",  debt.currentBalance());
        ctx.setVariable("currency",    debt.currency());
        ctx.setVariable("dueDate",     debt.dueDate().format(DATE_FORMAT));
        ctx.setVariable("description", debt.description());
        ctx.setVariable("debtId",      debt.id());

        String html = templateEngine.process("payment-reminder", ctx);
        sendEmail(debtor.email(), "Recordatorio: Tu deuda vence en 3 días - Ref #" + debt.id(), html);
    }

    private void sendEmail(String to, String subject, String html) {
        Email fromEmail = new Email(from);
        Email toEmail   = new Email(to);
        Content content = new Content("text/html", html);
        Mail mail       = new Mail(fromEmail, subject, toEmail, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getStatusCode() + " - " + response.getBody());
            }
            log.info("Correo enviado exitosamente a {} (status: {})", to, response.getStatusCode());
        } catch (IOException e) {
            log.error("Error al enviar correo a {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Fallo al enviar correo a " + to, e);
        }
    }
}
