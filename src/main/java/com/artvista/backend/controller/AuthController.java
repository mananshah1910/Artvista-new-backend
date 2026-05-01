package com.artvista.backend.controller;

import com.artvista.backend.model.User;
import com.artvista.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.artvista.backend.service.EmailService;
import com.artvista.backend.service.OtpService;
import com.artvista.backend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmailService emailService;
    @Autowired private OtpService otpService;
    @Autowired private JwtTokenProvider tokenProvider;

    // ─── REGISTER ────────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) user.setRole("visitor");

        User saved = userRepository.save(user);
        String otp = otpService.generateOtp(saved.getEmail());

        log.info("\n*** OTP for {} is: {} ***", saved.getEmail(), otp);

        try {
            String toEmail = saved.getEmail();
            String toName  = (saved.getName() == null || saved.getName().isBlank())
                             ? toEmail.split("@")[0] : saved.getName();
            if (toEmail != null) {
                emailService.sendWelcomeEmail(Objects.requireNonNull(toEmail),
                                              Objects.requireNonNull(toName),
                                              Objects.requireNonNull(otp));
            }
        } catch (Exception e) {
            log.error("Welcome email failed for {}: {}", saved.getEmail(), e.getMessage());
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("requiresFirstLoginOtp", true);
        resp.put("email", saved.getEmail());
        resp.put("message", "Registration successful. Please verify OTP sent to your email.");
        return ResponseEntity.status(202).body(resp);
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email    = credentials.get("email");
        String password = credentials.get("password");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            User u = userOpt.get();

            if (u.isFirstLogin()) {
                String otp = otpService.generateOtp(u.getEmail());
                log.info("\n*** LOGIN OTP for {} is: {} ***", u.getEmail(), otp);

                try {
                    String toEmail = u.getEmail();
                    String toName  = (u.getName() == null || u.getName().isBlank())
                                     ? (toEmail != null ? toEmail.split("@")[0] : "User") : u.getName();
                    if (toEmail != null) {
                        emailService.sendWelcomeEmail(Objects.requireNonNull(toEmail),
                                                      Objects.requireNonNull(toName),
                                                      Objects.requireNonNull(otp));
                    }
                } catch (Exception e) {
                    log.error("Login OTP email failed for {}: {}", u.getEmail(), e.getMessage());
                }

                Map<String, Object> resp = new HashMap<>();
                resp.put("requiresFirstLoginOtp", true);
                resp.put("email", u.getEmail());
                resp.put("message", "First login requires OTP verification.");
                return ResponseEntity.status(202).body(resp);
            }

            String token = tokenProvider.generateToken(u.getEmail(), u.getRole());
            return ResponseEntity.ok(Map.of("token", token, "user", u));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    // ─── VERIFY FIRST LOGIN OTP ───────────────────────────────────────────────────
    @PostMapping("/verify-first-login")
    public ResponseEntity<?> verifyFirstLogin(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp   = req.get("otp");

        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            u.setFirstLogin(false);
            userRepository.save(u);
            String token = tokenProvider.generateToken(u.getEmail(), u.getRole());
            return ResponseEntity.ok(Map.of("token", token, "user", u));
        }
        return ResponseEntity.status(401).body("User not found.");
    }

    // ─── FORGOT PASSWORD ──────────────────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        if (userRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body("User with this email does not exist.");
        }

        String otp = otpService.generateOtp(email);
        log.info("\n*** FORGOT-PWD OTP for {} is: {} ***", email, otp);

        try {
            if (email != null) {
                emailService.sendOtpEmail(Objects.requireNonNull(email), Objects.requireNonNull(otp));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
        return ResponseEntity.ok("OTP sent to your email. Valid for 10 minutes.");
    }

    // ─── RESET PASSWORD ───────────────────────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {
        String email       = req.get("email");
        String otp         = req.get("otp");
        String newPassword = req.get("newPassword");

        if (!otpService.validateOtp(email, otp)) {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            u.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(u);
            try {
                String name = (u.getName() != null) ? u.getName() : email.split("@")[0];
                if (email != null) {
                    emailService.sendNotificationEmail(Objects.requireNonNull(email),
                        "Password Changed Successfully",
                        "Hello " + name + ",\n\nYour ArtVista password was reset successfully. If you didn't do this, contact support immediately.");
                }
            } catch (Exception ignored) {}
            return ResponseEntity.ok("Password successfully reset.");
        }
        return ResponseEntity.badRequest().body("User not found.");
    }

    // ─── RESEND OTP ───────────────────────────────────────────────────────────────
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User with this email does not exist.");
        }

        User u   = userOpt.get();
        String otp = otpService.getCurrentOtp(email);
        log.info("\n*** RESEND OTP for {} is: {} ***", email, otp);

        try {
            if (email != null) {
                String toName = (u.getName() == null || u.getName().isBlank())
                                ? email.split("@")[0] : u.getName();
                if (u.isFirstLogin()) {
                    emailService.sendWelcomeEmail(Objects.requireNonNull(email),
                                                  Objects.requireNonNull(toName),
                                                  Objects.requireNonNull(otp));
                } else {
                    emailService.sendOtpEmail(Objects.requireNonNull(email), Objects.requireNonNull(otp));
                }
            }
        } catch (Exception e) {
            log.error("Resend OTP failed for {}: {}", email, e.getMessage());
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
        return ResponseEntity.ok("OTP sent to your email. Valid for 10 minutes.");
    }

    // ─── LIST USERS ───────────────────────────────────────────────────────────────
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    // ─── TEST EMAIL ───────────────────────────────────────────────────────────────
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email address is required.");
        }
        try {
            emailService.sendNotificationEmail(Objects.requireNonNull(email),
                "ArtVista SMTP Test",
                "This is a test email from your ArtVista backend.\n\nTimestamp: " + java.time.LocalDateTime.now());
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Test email dispatched to " + email + ". Check your Spam/Promotions tab if not in inbox."
            ));
        } catch (Exception e) {
            log.error("SMTP test failed", e);
            return ResponseEntity.status(500).body("SMTP Test Failed: " + e.getMessage());
        }
    }

}
