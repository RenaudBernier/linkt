package com.linkt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Sends a verification code to the user's email for account activation.
     * Currently logs to console - can be extended to send actual emails.
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

        // TODO: Replace with actual email sending implementation
        logEmail(toEmail, subject, body);
    }

    /**
     * Sends a 2FA code to the user's email for login verification.
     * Currently logs to console - can be extended to send actual emails.
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

        // TODO: Replace with actual email sending implementation
        logEmail(toEmail, subject, body);
    }

    /**
     * Logs email content to console.
     * This is a placeholder that can be replaced with actual email sending logic.
     *
     * To implement real email sending:
     * 1. Add spring-boot-starter-mail dependency to pom.xml
     * 2. Configure SMTP settings in application.properties
     * 3. Inject JavaMailSender and use SimpleMailMessage or MimeMessage
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
