package bg.academy.outdoor.email;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {
    public void sendEmail(String to, String subject, String body) {
        String username = requiredEnv("SMTP_USERNAME");
        String password = requiredEnv("SMTP_PASSWORD");
        String host = requiredEnv("SMTP_HOST");
        String port = requiredEnv("SMTP_PORT");

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing required environment variable: " + name);
        }

        return value;
    }
}
