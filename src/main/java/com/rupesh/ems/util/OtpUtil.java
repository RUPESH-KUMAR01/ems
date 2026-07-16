package com.rupesh.ems.util;

import jakarta.ws.rs.InternalServerErrorException;
import java.security.SecureRandom;

public final class OtpUtil {
  private static final SecureRandom RANDOM = new SecureRandom();

  public OtpUtil() {}

  public static String generateOtp(int length) {
    if (length <= 0) {
      throw new InternalServerErrorException("Otp cannot be generated");
    }
    StringBuilder otp = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      otp.append(RANDOM.nextInt(10));
    }

    return otp.toString();
  }
}
