package com.pyokemon.payment.entity;

import java.time.LocalDateTime;

import com.pyokemon.common.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseEntity {

  private Long bookingId;
  private Long eventScheduleId;
  private String orderId;
  private String paymentKey;
  private String method;
  private Long amount;
  private PaymentStatus status;

  public enum PaymentStatus {
    READY, DONE, CANCELED, FAILED, EXPIRED
  }
}
