package com.linkt.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class VerificationService {

    private static final int CODE_EXPIRY_MINUTES = 5;

    public String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Generates 100000-999999
        return String.valueOf(code);
    }

    public LocalDateTime getCodeExpiry() {
        return LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES);
    }

    public boolean isCodeValid(String storedCode, LocalDateTime expiry, String providedCode) {
        if (storedCode == null || expiry == null || providedCode == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(expiry)) {
            return false; // Code expired
        }

        return storedCode.equals(providedCode);
    }
}
