package com.linkt.service;

import com.linkt.linkt.LinktApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = LinktApplication.class)
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void testSendVerificationEmail() {
        // Send a test verification email
        emailService.sendVerificationCode("renaudbernierjp@gmail.com", "Renaud", "123456");
        System.out.println("Verification email sent!");
    }

    @Test
    public void testSend2FAEmail() {
        // Send a test 2FA email
        emailService.send2FACode("renaudbernierjp@gmail.com", "Renaud", "654321");
        System.out.println("2FA email sent!");
    }
}
