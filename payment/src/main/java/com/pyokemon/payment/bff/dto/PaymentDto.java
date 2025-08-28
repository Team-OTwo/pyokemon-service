package com.pyokemon.payment.bff.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
  private Long paymentId;
  private Long amount;
  private String method;
  private Enum status;
  private LocalDateTime updatedAt;
}
