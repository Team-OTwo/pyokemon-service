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

  private String credentialId;
  private String credentialExchangeId;
  private Long bookingId;
  private Long tenantId;
  private VcStatus status;

  public enum VcStatus {
    CREDENTIAL_ISSUED, CREDENTIAL_SENT, CREDENTIAL_RECEIVED
  }
}
