package com.pyokemon.did.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.EventInvitation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이벤트 초대장 생성 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInvitationResponse {
    @JsonProperty("event_id")
    private Long eventId;
    
    @JsonProperty("invitation_url")
    private String invitationUrl;
    
    @JsonProperty("oob_id")
    private String oobId;
    
    /**
     * EventInvitation 엔티티로 변환
     * 
     * @return EventInvitation 엔티티
     */
    public EventInvitation toEntity() {
        return EventInvitation.builder()
                .eventId(eventId)
                .invitationUrl(invitationUrl)
                .oobId(oobId)
                .isValid(true)
                .build();
    }
}
