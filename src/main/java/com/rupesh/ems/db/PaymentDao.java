package com.rupesh.ems.db;

import com.rupesh.ems.core.Payment;
import io.dropwizard.hibernate.AbstractDAO;
import java.util.List;
import java.util.Optional;
import org.hibernate.SessionFactory;

public class PaymentDao extends AbstractDAO<Payment> {

  public PaymentDao(SessionFactory sessionFactory) {
    super(sessionFactory);
  }

  public Payment create(Payment payment) {
    return persist(payment);
  }

  public Payment update(Payment payment) {
    return currentSession().merge(payment);
  }

  public Optional<Payment> getById(Long id) {
    return Optional.ofNullable(get(id));
  }

  public Optional<Payment> findByRegistrationId(Long registrationId) {
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
    return currentSession().createQuery("FROM Payment", Payment.class).getResultList();
  }

  public boolean delete(Payment payment) {
    if (payment == null) {
      return false;
    }

    currentSession().remove(payment);
    return true;
  }
}
