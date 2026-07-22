package com.rupesh.ems.api.admin.res;

import java.math.BigDecimal;
import java.util.Map;

public class SystemStatsResponse {

  private long totalUsers;
  private Map<String, Long> usersByRole;

  private long totalEvents;
  private Map<String, Long> eventsByStatus;

  private long totalTeams;

  private long totalRegistrations;
  private Map<String, Long> registrationsByStatus;

  private long totalPayments;
  private Map<String, Long> paymentsByStatus;
  private BigDecimal totalRevenue;

  public SystemStatsResponse() {}

  public SystemStatsResponse(
      long totalUsers,
      Map<String, Long> usersByRole,
      long totalEvents,
      Map<String, Long> eventsByStatus,
      long totalTeams,
      long totalRegistrations,
      Map<String, Long> registrationsByStatus,
      long totalPayments,
      Map<String, Long> paymentsByStatus,
      BigDecimal totalRevenue) {
    this.totalUsers = totalUsers;
    this.usersByRole = usersByRole;
    this.totalEvents = totalEvents;
    this.eventsByStatus = eventsByStatus;
    this.totalTeams = totalTeams;
    this.totalRegistrations = totalRegistrations;
    this.registrationsByStatus = registrationsByStatus;
    this.totalPayments = totalPayments;
    this.paymentsByStatus = paymentsByStatus;
    this.totalRevenue = totalRevenue;
  }

  public long getTotalUsers() {
    return totalUsers;
  }

  public void setTotalUsers(long totalUsers) {
    this.totalUsers = totalUsers;
  }

  public Map<String, Long> getUsersByRole() {
    return usersByRole;
  }

  public void setUsersByRole(Map<String, Long> usersByRole) {
    this.usersByRole = usersByRole;
  }

  public long getTotalEvents() {
    return totalEvents;
  }

  public void setTotalEvents(long totalEvents) {
    this.totalEvents = totalEvents;
  }

  public Map<String, Long> getEventsByStatus() {
    return eventsByStatus;
  }

  public void setEventsByStatus(Map<String, Long> eventsByStatus) {
    this.eventsByStatus = eventsByStatus;
  }

  public long getTotalTeams() {
    return totalTeams;
  }

  public void setTotalTeams(long totalTeams) {
    this.totalTeams = totalTeams;
  }

  public long getTotalRegistrations() {
    return totalRegistrations;
  }

  public void setTotalRegistrations(long totalRegistrations) {
    this.totalRegistrations = totalRegistrations;
  }

  public Map<String, Long> getRegistrationsByStatus() {
    return registrationsByStatus;
  }

  public void setRegistrationsByStatus(Map<String, Long> registrationsByStatus) {
    this.registrationsByStatus = registrationsByStatus;
  }

  public long getTotalPayments() {
    return totalPayments;
  }

  public void setTotalPayments(long totalPayments) {
    this.totalPayments = totalPayments;
  }

  public Map<String, Long> getPaymentsByStatus() {
    return paymentsByStatus;
  }

  public void setPaymentsByStatus(Map<String, Long> paymentsByStatus) {
    this.paymentsByStatus = paymentsByStatus;
  }

  public BigDecimal getTotalRevenue() {
    return totalRevenue;
  }

  public void setTotalRevenue(BigDecimal totalRevenue) {
    this.totalRevenue = totalRevenue;
  }
}
