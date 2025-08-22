package com.pyokemon.event.bff.dto;

import lombok.Data;

@Data
public class BffEventDto {
    private Long eventId;
    private String title;
    private String thumbnailUrl;
}
