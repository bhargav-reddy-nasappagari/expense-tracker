package com.expensetracker.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class EmailUtil {

    // LOAD CREDENTIALS FROM THE ENVIRONMENT VARIABLES OR CONFIG
        private static final String SMTP_HOST = ConfigLoader.get("email.smtp.host");
        private static final String SMTP_PORT = ConfigLoader.get("email.smtp.port");
        private static final String USERNAME = ConfigLoader.get("email.smtp.username");
        private static final String PASSWORD = ConfigLoader.get("email.smtp.password");
        private static final String FROM_NAME = ConfigLoader.get("email.from.name", "ExpenseTracker");   

    /**
     * 1. Password Reset
     */
    public static void sendResetEmail(String toEmail, String resetLink) {

        String subject = "Password Reset Request";
        String htmlContent = "<h3>Password Reset</h3>" +
                "<p>Click the link below to reset your password:</p>" +
                "<p><a href=\"" + resetLink + "\">Reset Password</a></p>" +
                "<p>Link expires in 1 hour.</p>";

        // Run in background
        sendAsync(toEmail, subject, htmlContent);
    }

    /**
     * 2. NEW Feature: Account Verification
     */
    public static void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify your " + FROM_NAME + " Account";
        
        // Use ConfigLoader for base URL
        String baseUrl = ConfigLoader.get("app.base.url");
        String verifyLink = baseUrl + "/verify-email?token=" + token;

        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>Welcome to Expense Tracker!</h2>
                <p>Please click the link below to verify your email address and activate your account:</p>
                <div style="margin: 25px 0;">
                    <a href="%s" style="background-color: #546236; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">
                        VERIFY MY ACCOUNT
                    </a>
                </div>
                <p>If the button doesn't work, copy and paste this link:</p>
                <p><a href="%s">%s</a></p>
                <br>
                <p style="color: #666;"><i>Link expires in 24 hours.</i></p>
            </body>
            </html>
            """.formatted(verifyLink, verifyLink, verifyLink);

        // Run in background
        sendAsync(toEmail, subject, htmlContent);
    }

    // ================= PRIVATE HELPERS =================

    /**
     * Helper to run email sending in the background to avoid blocking the Servlet.
     */
    private static void sendAsync(String toEmail, String subject, String htmlContent) {
        CompletableFuture.runAsync(() -> {
            try {
                sendEmailRaw(toEmail, subject, htmlContent);
            } catch (Exception e) {
                e.printStackTrace(); // In production, use a Logger (SLF4J)
                System.err.println("Failed to send email to " + toEmail);
            }
        });
    }

    /**
     * Generic SMTP sending logic (Shared by both features)
     */
    private static void sendEmailRaw(String toEmail, String subject, String htmlContent) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
        System.out.println("Email sent to: " + toEmail + " | Type: " + subject);
    }
}