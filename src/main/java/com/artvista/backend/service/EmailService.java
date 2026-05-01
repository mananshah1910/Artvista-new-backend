package com.artvista.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailException;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String BRAND_COLOR = "#6366f1"; // Modern Indigo
    private static final String BRAND_NAME = "ArtVista Gallery";

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
        try {
            if (toEmail == null || subject == null || htmlBody == null) return;
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, BRAND_NAME));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            
            String pt = htmlBody.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim();
            helper.setText(Objects.requireNonNull(pt), Objects.requireNonNull(htmlBody));
            
            mailSender.send(message);
            log.info("✅ Email successfully sent to {}", toEmail);
            System.out.println("✅ [EmailService] SUCCESS: Email sent to " + toEmail);
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.error("❌ Email delivery failure to {}. Reason: {}", toEmail, e.getMessage());
            System.err.println("❌ [EmailService] ERROR: Failed to send email to " + toEmail + " | " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("❌ Unexpected runtime error during email delivery to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
