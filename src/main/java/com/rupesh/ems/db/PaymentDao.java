package com.rupesh.ems.db;

import com.rupesh.ems.core.Payment;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaymentDao extends AbstractDAO<Payment> {
  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentDao.class);

  public PaymentDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Payment create(Payment payment) {
    LOGGER.info(
        "DAO: Creating payment registrationId={} amount={} providerOrderId={}",
        payment.getRegistrationId(),
        payment.getAmount(),
        payment.getProviderOrderId());
    return persist(payment);
  }

  public Payment update(Payment payment) {
    LOGGER.info("DAO: Updating payment id={} status={}", payment.getId(), payment.getStatus());
    return currentSession().merge(payment);
  }

  public Optional<Payment> getById(Long id) {
    LOGGER.debug("DAO: Fetching payment by id={}", id);
    return Optional.ofNullable(get(id));
  }

  public Optional<Payment> findByRegistrationId(Long registrationId) {
    LOGGER.debug("DAO: Fetching payment by registrationId={}", registrationId);
    return currentSession()
        .createQuery(
            """
            FROM Payment
            WHERE registrationId = :registrationId
            """,
            Payment.class)
        .setParameter("registrationId", registrationId)
        .uniqueResultOptional();
  }

  public Optional<Payment> findByProviderOrderId(String providerOrderId) {
    LOGGER.debug("DAO: Fetching payment by providerOrderId={}", providerOrderId);
    return currentSession()
        .createQuery(
            """
            FROM Payment
            WHERE providerOrderId = :providerOrderId
            """,
            Payment.class)
        .setParameter("providerOrderId", providerOrderId)
        .uniqueResultOptional();
  }

  public List<Payment> findAll() {
    LOGGER.debug("DAO: Fetching all payments");
    return currentSession().createQuery("FROM Payment", Payment.class).getResultList();
  }

  public boolean delete(Payment payment) {
    if (payment == null) {
      return false;
    }
    LOGGER.info("DAO: Deleting payment id={}", payment.getId());
    currentSession().remove(payment);
    return true;
  }
}
