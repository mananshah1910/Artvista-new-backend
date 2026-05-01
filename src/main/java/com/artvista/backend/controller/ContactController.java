package com.artvista.backend.controller;

import com.artvista.backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private com.artvista.backend.repository.UserInquiryRepository userInquiryRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody com.artvista.backend.model.UserInquiry inquiry) {
        inquiry.setSubmittedAt(LocalDateTime.now());
        inquiry.setRead(false);
        userInquiryRepository.save(inquiry);

        // Send notification email via SMTP
        try {
            emailService.sendNotificationEmail(
                "mananshah1918@gmail.com", // Admin email
                "New Gallery Inquiry: " + inquiry.getSubject(),
                "You have received a new inquiry from " + inquiry.getName() + " (" + inquiry.getEmail() + "):\n\n" + inquiry.getMessage()
            );
        } catch (Exception e) {
            // Log error but don't fail the request
            System.err.println("Failed to send inquiry notification: " + e.getMessage());
        }

        return ResponseEntity.ok("Message received. We will get back to you soon!");
    }

    @GetMapping
    public java.util.List<com.artvista.backend.model.UserInquiry> getAllMessages() {
        return userInquiryRepository.findAll();
    }
}
