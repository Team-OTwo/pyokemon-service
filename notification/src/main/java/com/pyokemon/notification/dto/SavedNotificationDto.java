package com.pyokemon.notification.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedNotificationDto {
    private Long eventId;
    private Long accountId;
    private String title;
    private String message;
    private LocalDateTime ticketOpenAt;

}
