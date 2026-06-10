package com.rupesh.ems.service.Sms;

public interface SmsService {
  void sendOtp(String phoneNumber, String message);
}
