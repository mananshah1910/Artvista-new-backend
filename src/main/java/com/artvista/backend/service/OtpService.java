package com.artvista.backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // Store email -> OtpData
    private final Map<String, OtpData> otpCache = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final long EXPIRE_MINS = 10; // Increased to 10 minutes for reliability

    public String generateOtp(String email) {
        // Generate a fresh 6-digit OTP
        String otp = String.format("%06d", random.nextInt(1000000));
        long expirationTime = System.currentTimeMillis() + (EXPIRE_MINS * 60 * 1000);
        otpCache.put(email, new OtpData(otp, expirationTime));
        System.out.println("🔑 [OtpService] OTP generated for " + email + ": " + otp + " (Expires in 2 mins)");
        return otp;
    }

    public String getCurrentOtp(String email) {
        OtpData otpData = otpCache.get(email);
        if (otpData == null || System.currentTimeMillis() > otpData.getExpirationTime()) {
            return generateOtp(email);
        }
        return otpData.getOtp();
    }

    public boolean validateOtp(String email, String otp) {
        OtpData otpData = otpCache.get(email);
        if (otpData == null) {
            System.out.println("❌ [OtpService] Validation failed: No OTP found for " + email);
            return false;
        }
        
        if (System.currentTimeMillis() > otpData.getExpirationTime()) {
            otpCache.remove(email);
            System.out.println("❌ [OtpService] Validation failed: OTP expired for " + email);
            return false;
        }
        
        if (otpData.getOtp().equals(otp)) {
            otpCache.remove(email); // Success, clear it
            System.out.println("✅ [OtpService] OTP validated successfully for " + email);
            return true;
        }
        
        System.out.println("❌ [OtpService] Validation failed: Incorrect OTP for " + email);
        return false;
    }

    public void clearOtp(String email) {
        otpCache.remove(email);
    }

    private static class OtpData {
        private final String otp;
        private final long expirationTime;

        public OtpData(String otp, long expirationTime) {
            this.otp = otp;
            this.expirationTime = expirationTime;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpirationTime() {
            return expirationTime;
        }
    }
}
