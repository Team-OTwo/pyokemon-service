package com.pyokemon.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
  BOOKED("booked"), CANCELLED("cancelled"), PAYMENT_FAILED("payment-failed");

  private final String templateKey;
}
