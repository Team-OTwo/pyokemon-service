package com.pyokemon.event.dto.kafka;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventKafkaDto {
  private Long eventScheduleId;
  private String status;;
}
