package com.pyokemon.event.dto.kafka;


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
  Long eventId;
  Long accountId;
  String title;
  LocalDateTime ticketOpenAt;
}
