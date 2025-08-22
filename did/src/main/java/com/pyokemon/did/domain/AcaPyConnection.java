package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcaPyConnection extends BaseEntity {

    private Long id;
    private String connectionId;
    private String inviMsgId;
    private Long tenantId;
    private Long userId;
    private ConnectionStatus status;

    public enum ConnectionStatus {
        INITIAL,
        INVITATION_SENT,
        INVITATION_RECEIVED,
        REQUEST_SENT,
        REQUEST_RECEIVED,
        RESPONSE_SENT,
        RESPONSE_RECEIVED,
        COMPLETED,
        ABANDONED
    }
}
