package com.expensetracker.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.io.UnsupportedEncodingException;

public class EmailUtil {

    private static String getSmtpHost() {
        return ConfigLoader.get("email.smtp.host");
    }

    private static String getSmtpPort() {
        return ConfigLoader.get("email.smtp.port");
    }

    private static String getUsername() {
        return ConfigLoader.get("email.smtp.username");
    }

    private static String getPassword() {
        return ConfigLoader.get("email.smtp.password");
    }

    private static String getFromName() {
        return ConfigLoader.get("email.from.name", "ExpenseTracker");
    }

    /**
     * 1. Password Reset
     */
    public static void sendResetEmail(String toEmail, String resetLink) {

        String subject = "Password Reset Request – Expense Tracker";

        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2>Reset Your Password</h2>
                <p>Hello,</p>
                <p>You requested a password reset for your Expense Tracker account.</p>
                <p style="margin: 30px 0;">
                    <a href="%s" style="background-color: #4a6c3d; color: white; padding: 12px 28px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                        Reset Password
                    </a>
                </p>
                <p>This link will expire in 1 hour.</p>
                <p style="margin-top: 40px; font-size: 0.9em; color: #666;">
                    If you did not request a password reset, please ignore this email.<br>
                    <a href="{{unsubscribe_url}}" style="color: #666;">Unsubscribe from these notifications</a>
                </p>
            </body>
            </html>
            """.formatted(resetLink);

        sendAsync(toEmail, subject, htmlContent);
    }

    /**
     * 2. Account Verification
     */
    public static void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify Your Expense Tracker Account";

        String baseUrl = ConfigLoader.get("app.base.url", "http://localhost:8080/expense-tracker");
        String verifyLink = baseUrl + "/verify-email?token=" + token;

        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <h2>Welcome to Expense Tracker!</h2>
                <p>Thank you for signing up. Please verify your email address to activate your account.</p>
                <p style="margin: 30px 0;">
                    <a href="%s" style="background-color: #4a6c3d; color: white; padding: 12px 28px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block;">
                        Verify My Email
                    </a>
                </p>
                <p>If the button doesn't work, copy and paste this link:</p>
                <p><a href="%s">%s</a></p>
                <p style="margin-top: 30px; font-size: 0.95em;">This link expires in 24 hours.</p>
                <p style="margin-top: 40px; font-size: 0.9em; color: #666;">
                    If you didn't create an account, please ignore this email.<br>
                    <a href="{{unsubscribe_url}}" style="color: #666;">Unsubscribe from these notifications</a>
                </p>
            </body>
            </html>
            """.formatted(verifyLink, verifyLink, verifyLink);

        sendAsync(toEmail, subject, htmlContent);
    }

    // ────────────────────────────────────────────────
    //                Private Helpers
    // ────────────────────────────────────────────────

    private static void sendAsync(String toEmail, String subject, String htmlContent) {
        CompletableFuture.runAsync(() -> {
            try {
                sendEmailRaw(toEmail, subject, htmlContent);
            } catch (MessagingException | UnsupportedEncodingException e) {
                System.err.println("Failed to send email to " + toEmail);
                System.err.println("Subject: " + subject);
                e.printStackTrace();
            }
        });
    }

    private static void sendEmailRaw(String toEmail, String subject, String htmlContent) throws MessagingException, UnsupportedEncodingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", getSmtpHost());
        props.put("mail.smtp.port", getSmtpPort());

        // Uncomment the next line only when troubleshooting connection / TLS issues
        // props.put("mail.debug", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(getUsername(), getPassword());
            }
        });

        Message message = new MimeMessage(session);

        // Better From header: "ExpenseTracker <your-email@...>"
        // Replace the from block with:
        InternetAddress from;
        try {
            from = new InternetAddress(getUsername(), getFromName(), "UTF-8");  // explicitly specify charset
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to create From address", e);
        }
        message.setFrom(from);

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);

        System.out.println("Email sent successfully → " + toEmail + " | " + subject);
    }
}