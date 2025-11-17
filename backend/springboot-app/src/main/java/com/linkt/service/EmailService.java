package com.linkt.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    /**
     * Sends a verification code to the user's email for account activation.
     *
     * @param toEmail recipient email address
     * @param firstName recipient's first name
     * @param code 6-digit verification code
     */
    public void sendVerificationCode(String toEmail, String firstName, String code) {
        String subject = "Verify Your Linkt Account";
        String body = String.format("""
            Hi %s,

            Your email verification code is: %s

            This code will expire in 5 minutes.

            If you didn't create this account, please ignore this email.

            Best regards,
            The Linkt Team
            """, firstName, code);

        sendEmail(toEmail, subject, body);
    }

    /**
     * Sends a 2FA code to the user's email for login verification.
     *
     * @param toEmail recipient email address
     * @param firstName recipient's first name
     * @param code 6-digit 2FA code
     */
    public void send2FACode(String toEmail, String firstName, String code) {
        String subject = "Your Linkt Login Code";
        String body = String.format("""
            Hi %s,

            Your login verification code is: %s

            This code will expire in 5 minutes.

            If you didn't request this code, please secure your account immediately.

            Best regards,
            The Linkt Team
            """, firstName, code);

        sendEmail(toEmail, subject, body);
    }

    /**
     * Sends an email using SendGrid API.
     * If SendGrid API key is not configured, logs email to console instead.
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param body email body content
     */
    private void sendEmail(String toEmail, String subject, String body) {
        // If SendGrid API key is not configured, fall back to logging
        if (sendGridApiKey == null || sendGridApiKey.trim().isEmpty()) {
            logger.warn("SendGrid API key not configured. Email will be logged instead of sent.");
            logEmail(toEmail, subject, body);
            return;
        }

        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Email sent successfully to: {}", toEmail);
            } else {
                logger.error("Failed to send email. Status code: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            logger.error("Error sending email to {}: {}", toEmail, e.getMessage());
            // Log the email as fallback
            logEmail(toEmail, subject, body);
        }
    }

    /**
     * Logs email content to console.
     * This is used as a fallback when SendGrid is not configured.
     *
     * @param toEmail recipient email address
     * @param subject email subject
     * @param body email body content
     */
    private void logEmail(String toEmail, String subject, String body) {
        logger.info("\n" +
                "========================================\n" +
                "EMAIL TO: {}\n" +
                "SUBJECT: {}\n" +
                "----------------------------------------\n" +
                "{}\n" +
                "========================================\n",
                toEmail, subject, body);
    }
}
