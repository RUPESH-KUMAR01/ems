package com.rupesh.ems.service;

import com.rupesh.ems.core.User;
import com.rupesh.ems.core.VerificationCode;
import com.rupesh.ems.core.VerificationType;
import com.rupesh.ems.db.UserDao;
import com.rupesh.ems.db.VerificationDao;
import com.rupesh.ems.exceptions.ConflictException;
import com.rupesh.ems.exceptions.NotFoundException;
import com.rupesh.ems.exceptions.UnauthorizedException;
import com.rupesh.ems.exceptions.UnprocessableEntityException;
import com.rupesh.ems.service.email.EmailService;
import com.rupesh.ems.service.sms.SmsService;
import com.rupesh.ems.util.OtpUtil;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class VerificationService {

  private static final int OTP_LENGTH = 6;
  private static final int OTP_EXPIRY_MINUTES = 10;
  private final VerificationDao verificationDao;
  private final UserDao userDao;
  private final EmailService emailService;
  private final SmsService smsService;

  public VerificationService(
      VerificationDao verificationDao,
      UserDao userDao,
      EmailService emailService,
      SmsService smsService) {
    this.verificationDao = verificationDao;
    this.userDao = userDao;
    this.emailService = emailService;
    this.smsService = smsService;
  }

  public void generatePhoneOtp(Long userId) {
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not Found"));

    if (user.isPhoneVerified()) {
      throw new ConflictException("Phone already Verified");
    }
    String otp = OtpUtil.generateOtp(OTP_LENGTH);

    verificationDao.deleteByUserAndType(userId, VerificationType.PHONE);

    VerificationCode code =
        new VerificationCode(
            userId,
            VerificationType.PHONE,
            otp,
            Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES));

    verificationDao.create(code);
    smsService.sendOtp(
        user.getPhone(),
        """
                EMS Verification Code: %s

                This code expires in %d minutes.

                Do not share this code with anyone.
                """
            .formatted(otp, OTP_EXPIRY_MINUTES));
  }

  public void generateEmailOtp(Long userId) {
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not Found"));

    if (user.isEmailVerified()) {
      throw new ConflictException("Email already Verified");
    }
    String otp = OtpUtil.generateOtp(OTP_LENGTH);

    verificationDao.deleteByUserAndType(userId, VerificationType.EMAIL);

    VerificationCode code =
        new VerificationCode(
            userId,
            VerificationType.EMAIL,
            otp,
            Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES));

    verificationDao.create(code);
    emailService.sendEmail(
        user.getEmail(),
        "Verify Your EMS Account",
        """
                Hello %s,

                Welcome to EMS.

                Your email verification code is:

                %s

                This code will expire in %d minutes.

                If you did not request this verification, please ignore this email.

                Regards,
                EMS Team
                """
            .formatted(user.getName(), otp, OTP_EXPIRY_MINUTES));
  }

  public void verifyPhone(Long userId, String otp) {
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (user.isPhoneVerified()) {
      throw new ConflictException("Phone already Verified");
    }

    VerificationCode verificationCode =
        verificationDao
            .findByUserAndType(user.getId(), VerificationType.PHONE)
            .orElseThrow(() -> new NotFoundException("Otp not generated"));

    if (verificationCode.isExpired()) {
      throw new UnprocessableEntityException("Otp expired");
    }
    if (verificationCode.getOtp().equals(otp)) {
      user.setPhoneVerified(true);
    } else {
      throw new UnauthorizedException("Invalid Otp");
    }
    userDao.update(user);
    verificationDao.delete(verificationCode);
  }

  public void verifyEmail(Long userId, String otp) {
    User user =
        userDao.getUserById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (user.isEmailVerified()) {
      throw new ConflictException("Email already verified");
    }

    VerificationCode verificationCode =
        verificationDao
            .findByUserAndType(user.getId(), VerificationType.EMAIL)
            .orElseThrow(() -> new NotFoundException("Otp not generated"));

    if (verificationCode.isExpired()) {
      throw new UnprocessableEntityException("Otp expired");
    }
    if (verificationCode.getOtp().equals(otp)) {
      user.setEmailVerified(true);
    } else {
      throw new UnauthorizedException("Invalid Otp");
    }
    userDao.update(user);
    verificationDao.delete(verificationCode);
  }
}
