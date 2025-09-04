package com.pyokemon.notification.entity;


import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavedNotification {
    private Long savedNotificationId;
    private Long eventId;
    private Long accountId;
    private String title;
    private String message;
    private LocalDateTime ticketOpenAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
