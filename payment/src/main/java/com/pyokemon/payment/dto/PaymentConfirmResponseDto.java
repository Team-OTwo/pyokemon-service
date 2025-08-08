package com.pyokemon.payment.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.pyokemon.payment.entity.Payment;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmResponseDto {
  private String paymentKey;
  private String method;
  private int amount;
  private String status;
  private OffsetDateTime approvedAt;

}
