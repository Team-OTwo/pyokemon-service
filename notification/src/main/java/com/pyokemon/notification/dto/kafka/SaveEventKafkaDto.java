package com.pyokemon.notification.dto.kafka;


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
    private Long eventId;
    private Long accountId;
    private String title;
    private LocalDateTime ticketOpenAt;
}
