package com.rupesh.ems.service.Sms;

public interface SmsService {
  public void sendOtp(String phoneNumber, String message);
}
