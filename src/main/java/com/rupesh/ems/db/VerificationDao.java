package com.rupesh.ems.db;

import com.rupesh.ems.core.VerificationCode;
import com.rupesh.ems.core.VerificationType;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.Optional;
import org.hibernate.SessionFactory;

public class VerificationDao extends AbstractDAO<VerificationCode> {

  public VerificationDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public VerificationCode create(VerificationCode verificationCode) {
    return persist(verificationCode);
  }

  public Optional<VerificationCode> findByUserAndType(Long userId, VerificationType type) {
    return currentSession()
        .createQuery(
            "FROM VerificationCode " + "WHERE userId = :userId " + "AND type = :type",
            VerificationCode.class)
        .setParameter("userId", userId)
        .setParameter("type", type)
        .uniqueResultOptional();
  }

  public void delete(VerificationCode verificationCode) {

    currentSession().remove(verificationCode);
  }

  public void deleteByUserAndType(Long userId, VerificationType type) {

    findByUserAndType(userId, type).ifPresent(this::delete);
  }
}
