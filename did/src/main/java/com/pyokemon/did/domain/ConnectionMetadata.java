package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class ConnectionMetadata extends BaseEntity {

    private String connId;
    private String inviMsgId;
    private Long userId;
    private String deviceId;
    private Long tenantId;
    private ConnectionStatus status;

    public enum ConnectionStatus {
        PENDING,
        ACTIVE,
        VC_ISSUED,
        REVOKED
    }

}
