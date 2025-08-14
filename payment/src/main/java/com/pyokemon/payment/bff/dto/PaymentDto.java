package com.pyokemon.payment.bff.dto;

import lombok.*;

import java.time.LocalDateTime;

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
