package com.pyokemon.payment.entity;

import java.time.LocalDateTime;

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
public class Payment {

  private Long paymentId;
  private Long bookingId;
  private String orderId;
  private String paymentKey;
  private String method;
  private Long amount;
  private PaymentStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public enum PaymentStatus {
    READY, DONE, CANCELED, FAILED, EXPIRED
  }

}
