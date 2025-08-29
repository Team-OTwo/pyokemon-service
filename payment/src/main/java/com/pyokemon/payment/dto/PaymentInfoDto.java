package com.pyokemon.payment.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoDto {
  private Long paymentId;
  private Long amount;
  private String method;
  private Enum status;
  private LocalDateTime updatedAt;
}
