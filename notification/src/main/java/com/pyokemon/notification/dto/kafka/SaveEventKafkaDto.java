package com.pyokemon.notification.dto.kafka;


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveEventKafkaDto {
  private Long eventId;
  private Long accountId;
  private String title;
  private LocalDateTime ticketOpenAt;
}
