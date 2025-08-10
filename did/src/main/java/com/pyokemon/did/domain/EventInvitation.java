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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInvitation extends BaseEntity {
    private Long eventId;
    private Long tenantId;
    private String invitationUrl;
    private String oobId;
    private boolean isValid = true;
}