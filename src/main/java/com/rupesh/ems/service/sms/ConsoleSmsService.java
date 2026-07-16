package com.rupesh.ems.service.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleSmsService implements SmsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleSmsService.class);

  @Override
  public void sendOtp(String phoneNumber, String message) {

    LOGGER.info(
        """
                =====================================
                SMS SENT
                To      : {}
                Message :
                {}
                =====================================
                """,
        phoneNumber,
        message);
  }
}
