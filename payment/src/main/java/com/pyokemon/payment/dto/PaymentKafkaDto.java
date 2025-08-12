package com.pyokemon.payment.dto;


import com.pyokemon.payment.entity.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentKafkaDto {
  private Long paymentId;
  private Long bookingId;
  Payment.PaymentStatus status;
}
