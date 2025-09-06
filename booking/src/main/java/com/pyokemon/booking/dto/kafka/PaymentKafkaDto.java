package com.pyokemon.booking.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentKafkaDto {
  private Long id;
  private Long bookingId;
  private String status;
}
