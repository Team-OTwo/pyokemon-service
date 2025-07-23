package com.pyokemon.event.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatClass {
    private Long seatClassId;
    private String className;
    private Integer priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
