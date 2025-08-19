package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantEventListResponseDtoForApp {
    private List<TenantEventListDto> events;
    private LocalDateTime lastCursorDate;
    private Long lastCursorId;
}
