package com.pyokemon.payment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequestDto {
  private String paymentKey;
  private String orderId;
  private int amount;
  private String method;
}
