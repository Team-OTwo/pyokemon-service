package com.pyokemon.did.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이벤트 초대장 생성 요청 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventInvitationRequest {
    @NotNull
    @JsonProperty("event_id")
    private Long eventId;
    
    @NotNull
    private String title;
    
    private String description;
}
