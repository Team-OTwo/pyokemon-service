package com.pyokemon.event.dto.kafka;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
