package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 이벤트 초대장 정보를 저장하는 엔티티
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventInvitation extends BaseEntity {
    private Long eventId;
    private Long tenantId;
    private String invitationUrl;
    private String oobId;
    //private String inviMsgId;
    private boolean isValid;
    
    @Builder
    public EventInvitation(Long eventId, Long tenantId, String invitationUrl, String oobId) {
        this.eventId = eventId;
        this.tenantId = tenantId;
        this.invitationUrl = invitationUrl;
        this.oobId = oobId;
        // isValid는 기본값 true 사용
        this.isValid = true;
    }
}