package com.pyokemon.event.dto.bff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BffEventDto {
    private Long id;
    private String title;
    private String thumbnailUrl;
}