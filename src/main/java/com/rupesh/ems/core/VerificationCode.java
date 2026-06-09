package com.rupesh.ems.core;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "verification_codes",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"userId", "type"})})
public class VerificationCode {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private VerificationType type;

  @Column(nullable = false)
  private String otp;

  @Column(nullable = false)
  private Instant expiresAt;

  public VerificationCode() {}

  public VerificationCode(Long userId, VerificationType type, String otp, Instant expiresAt) {
    this.userId = userId;
    this.type = type;
    this.otp = otp;
    this.expiresAt = expiresAt;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public VerificationType getType() {
    return type;
  }

  public String getOtp() {
    return otp;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public void setType(VerificationType type) {
    this.type = type;
  }

  public void setOtp(String otp) {
    this.otp = otp;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }
}
