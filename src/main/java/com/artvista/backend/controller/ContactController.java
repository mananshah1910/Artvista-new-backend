package com.artvista.backend.controller;

import com.artvista.backend.model.UserInquiry;
import com.artvista.backend.repository.UserInquiryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    @Autowired
    private UserInquiryRepository userInquiryRepository;

    @PostMapping
    public ResponseEntity<?> sendMessage(@RequestBody UserInquiry inquiry) {
        inquiry.setSubmittedAt(LocalDateTime.now());
        inquiry.setRead(false);
        userInquiryRepository.save(inquiry);
        return ResponseEntity.ok("Message received. We will get back to you soon!");
    }

    @GetMapping
    public List<UserInquiry> getAllMessages() {
        return userInquiryRepository.findAll();
    }
}
