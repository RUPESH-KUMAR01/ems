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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(VerificationService.class);

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
        userDao
            .getUserById(userId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("User not found for ID: {}", userId);
                  return new NotFoundException("User not Found");
                });

    if (user.isPhoneVerified()) {
      LOGGER.warn("Attempt to generate OTP for already verified phone for user ID: {}", userId);
      throw new ConflictException("Phone already Verified");
    }
    String otp = OtpUtil.generatePhoneOtp(OTP_LENGTH);

    verificationDao.deleteByUserAndType(userId, VerificationType.PHONE);
    LOGGER.info("Deleted existing phone verification code for user ID: {}", userId);
    VerificationCode code =
        new VerificationCode(
            userId,
            VerificationType.PHONE,
            otp,
            Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES));

    verificationDao.create(code);
    LOGGER.info("Created new phone verification code for user ID: {}", userId);
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
    LOGGER.info("Generating email OTP for user with ID: {}", userId);
    User user =
        userDao
            .getUserById(userId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("User not found for ID: {}", userId);
                  return new NotFoundException("User not Found");
                });

    if (user.isEmailVerified()) {
      LOGGER.warn("Attempt to generate OTP for already verified email for user ID: {}", userId);
      throw new ConflictException("Email already Verified");
    }
    String otp = OtpUtil.generateEmailOtp(OTP_LENGTH);

    verificationDao.deleteByUserAndType(userId, VerificationType.EMAIL);
    LOGGER.info("Deleted existing email verification code for user ID: {}", userId);

    VerificationCode code =
        new VerificationCode(
            userId,
            VerificationType.EMAIL,
            otp,
            Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES));

    verificationDao.create(code);
    LOGGER.info("Created new email verification code for user ID: {}", userId);
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
    LOGGER.info("Verifying phone for user with ID: {}", userId);
    User user =
        userDao
            .getUserById(userId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("User not found for ID: {}", userId);
                  return new NotFoundException("User not found");
                });

    if (user.isPhoneVerified()) {
      LOGGER.warn("Attempt to verify already verified phone for user ID: {}", userId);
      throw new ConflictException("Phone already Verified");
    }

    VerificationCode verificationCode =
        verificationDao
            .findByUserAndType(user.getId(), VerificationType.PHONE)
            .orElseThrow(
                () -> {
                  LOGGER.warn("Otp not generated for user ID: {}", userId);
                  return new NotFoundException("Otp not generated");
                });

    if (verificationCode.isExpired()) {
      LOGGER.warn("Otp expired for user ID: {}", userId);
      throw new UnprocessableEntityException("Otp expired");
    }
    if (verificationCode.getOtp().equals(otp)) {
      user.setPhoneVerified(true);
    } else {
      LOGGER.warn("Invalid Otp provided for user ID: {}", userId);
      throw new UnauthorizedException("Invalid Otp");
    }
    userDao.update(user);
    LOGGER.info("Phone verified successfully for user ID: {}", userId);
    verificationDao.delete(verificationCode);
    LOGGER.info("Deleted Phone verification code for user ID: {}", userId);
  }

  public void verifyEmail(Long userId, String otp) {
    LOGGER.info("Verifying email for user with ID: {}", userId);
    User user =
        userDao
            .getUserById(userId)
            .orElseThrow(
                () -> {
                  LOGGER.warn("User not found for ID: {}", userId);
                  return new NotFoundException("User not found");
                });

    if (user.isEmailVerified()) {
      LOGGER.warn("Attempt to verify already verified email for user ID: {}", userId);
      throw new ConflictException("Email already verified");
    }

    VerificationCode verificationCode =
        verificationDao
            .findByUserAndType(user.getId(), VerificationType.EMAIL)
            .orElseThrow(
                () -> {
                  LOGGER.warn("OTP not generated for user ID: {}", userId);
                  return new NotFoundException("Otp not generated");
                });

    if (verificationCode.isExpired()) {
      LOGGER.warn("OTP expired for user ID: {}", userId);
      throw new UnprocessableEntityException("Otp expired");
    }
    if (verificationCode.getOtp().equals(otp)) {
      user.setEmailVerified(true);
    } else {
      LOGGER.warn("Invalid OTP provided for user ID: {}", userId);
      throw new UnauthorizedException("Invalid Otp");
    }
    userDao.update(user);
    LOGGER.info("Email verified successfully for user ID: {}", userId);
    verificationDao.delete(verificationCode);
    LOGGER.info("Deleted Email verification code for user ID: {}", userId);
  }
}
