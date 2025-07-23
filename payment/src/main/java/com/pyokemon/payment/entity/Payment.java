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

  private Long payment_id;
  private Long booking_id;
  private Long user_id;
  private String payment_key;
  private String method;
  private Long total_price;
  private PaymentStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public enum PaymentStatus {
    READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED
  }

}
