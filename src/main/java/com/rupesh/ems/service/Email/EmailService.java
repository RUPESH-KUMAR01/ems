package com.rupesh.ems.service.Email;

public interface EmailService {
  void sendEmail(String to, String subject, String body);
}
