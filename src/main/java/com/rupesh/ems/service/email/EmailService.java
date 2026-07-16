package com.rupesh.ems.service.email;

public interface EmailService {
  void sendEmail(String to, String subject, String body);
}
