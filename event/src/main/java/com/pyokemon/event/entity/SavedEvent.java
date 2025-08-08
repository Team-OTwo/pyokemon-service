package com.pyokemon.event.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedEvent {
    private Long savedEventId;
    private Long eventId;
    private Long accountId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
