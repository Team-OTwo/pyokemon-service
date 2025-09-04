package com.pyokemon.payment.dto;

import java.time.LocalDateTime;

import com.pyokemon.payment.entity.Payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
  private Long id;
  private Long bookingId;
  private Long eventScheduleId;
  private String orderId;
  private String paymentKey;
  private int amount;
  private String method;
  private String status;
  private Long accountId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
