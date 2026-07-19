package com.rupesh.ems.service.email;

import com.rupesh.ems.configs.EmailServiceConfiguration;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SMTPEmailService implements EmailService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SMTPEmailService.class);

  private final String username;
  private final String password;

  public SMTPEmailService(EmailServiceConfiguration config) {
    this.username = config.getUsername();
    this.password = config.getPassword();
  }

  @Override
  public void sendEmail(String to, String subject, String body) {

    Properties props = new Properties();

    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");

    Session session =
        Session.getInstance(
            props,
            new Authenticator() {
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
      LOGGER.info("Email sent successfully to={}", to);

    } catch (Exception e) {
      LOGGER.error("Failed to send email to={}", to, e);
      throw new RuntimeException("Failed to send email", e);
    }
    
  }
}
