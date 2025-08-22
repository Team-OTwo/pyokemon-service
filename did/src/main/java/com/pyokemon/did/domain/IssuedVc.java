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
public class IssuedVc extends BaseEntity {

    private Long id;
    private String credentialExchangeId;
    private Long bookingId;
    private Long tenantId;
    private String credoConnId;
    private VcStatus status;

    public enum VcStatus {
        PROPOSAL_SENT,
        PROPOSAL_RECEIVED,
        OFFER_SENT,
        OFFER_RECEIVED,
        REQUEST_SENT,
        REQUEST_RECEIVED,
        CREDENTIAL_ISSUED,
        CREDENTIAL_RECEIVED,
        CREDENTIAL_ACKED,
        FAILED,
        ABANDONED
    }
}
