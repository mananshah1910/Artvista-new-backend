package com.artvista.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.io.IOException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${EMAIL_API_KEY:}")
    private String apiKey;

    @Value("${spring.mail.username:adityasingh01227@gmail.com}")
    private String fromEmail;

    private static final String BRAND_COLOR = "#6366f1"; // Modern Indigo
    private static final String BRAND_NAME = "ArtVista Gallery";
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private String getHtmlWrapper(String title, String content) {
        return "<div style=\"font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; padding: 40px 20px; background-color: #f8fafc; color: #1e293b;\">"
                + "<div style=\"max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);\">"
                + "  <div style=\"padding: 32px; background: " + BRAND_COLOR + "; text-align: center;\">"
                + "    <h1 style=\"color: #ffffff; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -0.025em;\">" + BRAND_NAME + "</h1>"
                + "  </div>"
                + "  <div style=\"padding: 40px 32px;\">"
                + "    <h2 style=\"margin-top: 0; color: #0f172a; font-size: 20px; font-weight: 700; text-align: center;\">" + title + "</h2>"
                + "    <div style=\"font-size: 16px; line-height: 1.6; color: #475569;\">"
                +        content
                + "    </div>"
                + "    <div style=\"margin-top: 32px; padding-top: 32px; border-top: 1px solid #f1f5f9; text-align: center; font-size: 14px; color: #94a3b8;\">"
                + "      <p style=\"margin: 0;\">Explore the beauty of art at ArtVista.</p>"
                + "      <p style=\"margin: 8px 0 0;\">&copy; 2024 ArtVista Gallery. All rights reserved.</p>"
                + "    </div>"
                + "  </div>"
                + "</div>"
                + "</div>";
    }

    public void sendWelcomeEmail(String toEmail, String name, String otp) {
        System.out.println("📧 [EmailService] Preparing Welcome Email for: " + toEmail);
        String title = "Welcome to the World of Art!";
        String content = "<p>Hello <strong>" + name + "</strong>,</p>"
                + "<p>We're thrilled to have you join <strong>ArtVista Gallery</strong>. Your journey into a world of creativity and inspiration starts here!</p>"
                + "<div style=\"margin: 32px 0; padding: 24px; background: #f1f5f9; border-radius: 12px; text-align: center;\">"
                + "  <p style=\"margin: 0 0 8px; font-size: 14px; text-transform: uppercase; letter-spacing: 0.05em; font-weight: 600; color: #64748b;\">Your Verification OTP</p>"
                + "  <span style=\"font-size: 36px; font-weight: 800; color: " + BRAND_COLOR + "; letter-spacing: 0.1em;\">" + otp + "</span>"
                + "  <p style=\"margin: 12px 0 0; font-size: 13px; color: #94a3b8;\">Valid for 10 minutes</p>"
                + "</div>"
                + "<p>Simply enter this code to verify your account and start exploring our curated collections.</p>"
                + "<p>Best regards,<br><strong>The ArtVista Team</strong></p>";

        sendEmail(toEmail, "Welcome to ArtVista!", getHtmlWrapper(title, content));
    }

    public void sendOtpEmail(String toEmail, String otp) {
        System.out.println("📧 [EmailService] Preparing OTP Email for: " + toEmail);
        String title = "Reset Your Password";
        String content = "<p>Hello,</p>"
                + "<p>We received a request to reset the password for your ArtVista account. Use the code below to proceed:</p>"
                + "<div style=\"margin: 32px 0; padding: 24px; background: #f1f5f9; border-radius: 12px; text-align: center;\">"
                + "  <span style=\"font-size: 36px; font-weight: 800; color: " + BRAND_COLOR + "; letter-spacing: 0.1em;\">" + otp + "</span>"
                + "  <p style=\"margin: 12px 0 0; font-size: 13px; color: #94a3b8;\">Valid for 10 minutes</p>"
                + "</div>"
                + "<p>If you didn't request this, you can safely ignore this email.</p>"
                + "<p>Best regards,<br><strong>The ArtVista Team</strong></p>";

        sendEmail(toEmail, "ArtVista - Password Reset Code", getHtmlWrapper(title, content));
    }

    public void sendNotificationEmail(String toEmail, String subject, String body) {
        System.out.println("📧 [EmailService] Preparing Notification Email for: " + toEmail + " | Subject: " + subject);
        String content = body != null ? body.replace("\n", "<br>") : "";
        sendEmail(toEmail, subject, getHtmlWrapper(subject, content));
    }

    private void sendEmail(String toEmail, String subject, String htmlBody) {
        if (toEmail == null || subject == null || htmlBody == null) return;
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ [EmailService] ERROR: EMAIL_API_KEY is not set. Cannot send email to " + toEmail);
            return;
        }

        try {
            ObjectNode payload = mapper.createObjectNode();
            
            ObjectNode sender = mapper.createObjectNode();
            sender.put("name", BRAND_NAME);
            sender.put("email", fromEmail);
            payload.set("sender", sender);
            
            ArrayNode toArray = mapper.createArrayNode();
            ObjectNode toObj = mapper.createObjectNode();
            toObj.put("email", toEmail);
            toArray.add(toObj);
            payload.set("to", toArray);
            
            payload.put("subject", subject);
            payload.put("htmlContent", htmlBody);

            RequestBody body = RequestBody.create(
                mapper.writeValueAsString(payload),
                MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                .url("https://api.brevo.com/v3/smtp/email")
                .addHeader("accept", "application/json")
                .addHeader("api-key", apiKey)
                .post(body)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("✅ Email successfully sent to {}", toEmail);
                    System.out.println("✅ [EmailService] SUCCESS: Email sent to " + toEmail);
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No response body";
                    log.error("❌ Email API failure. Code: {}, Body: {}", response.code(), errorBody);
                    System.err.println("❌ [EmailService] ERROR: Failed to send email to " + toEmail + " | Code: " + response.code() + " | " + errorBody);
                }
            }
        } catch (IOException e) {
            log.error("❌ Unexpected IO error during email delivery to {}: {}", toEmail, e.getMessage(), e);
            System.err.println("❌ [EmailService] ERROR: IO Exception " + e.getMessage());
        }
    }
}

