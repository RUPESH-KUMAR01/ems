package com.rupesh.ems.db;

import com.rupesh.ems.core.VerificationCode;
import com.rupesh.ems.core.VerificationType;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationDao extends AbstractDAO<VerificationCode> {
  private static final Logger LOGGER = LoggerFactory.getLogger(VerificationDao.class);

  public VerificationDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public VerificationCode create(VerificationCode verificationCode) {
    LOGGER.info(
        "DAO: Creating verification code for userId={} type={}",
        verificationCode.getUserId(),
        verificationCode.getType());
    return persist(verificationCode);
  }

  public Optional<VerificationCode> findByUserAndType(Long userId, VerificationType type) {
    LOGGER.debug("DAO: Fetching verification code for userId={} type={}", userId, type);
    return currentSession()
        .createQuery(
            "FROM VerificationCode WHERE userId = :userId AND type = :type", VerificationCode.class)
        .setParameter("userId", userId)
        .setParameter("type", type)
        .uniqueResultOptional();
  }

  public void delete(VerificationCode verificationCode) {
    if (verificationCode != null) {
      LOGGER.info(
          "DAO: Deleting verification code for userId={} type={}",
          verificationCode.getUserId(),
          verificationCode.getType());
      currentSession().remove(verificationCode);
    }
  }

  public void deleteByUserAndType(Long userId, VerificationType type) {
    LOGGER.info("DAO: Deleting verification code for userId={} type={}", userId, type);
    findByUserAndType(userId, type).ifPresent(this::delete);
  }
}
