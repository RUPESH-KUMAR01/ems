package com.rupesh.ems.service.sms;

public interface SmsService {
  void sendOtp(String phoneNumber, String message);
}
