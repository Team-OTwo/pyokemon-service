package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcaPyConnection extends BaseEntity {

    private String connectionId;
    private String inviMsgId;
    private Long tenantId;
    private Long userId;
    private ConnectionStatus status;

    public enum ConnectionStatus {
        PENDING,
        ACTIVE,
        REVOKED
    }
}
