package com.pyokemon.did.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.common.entity.BaseEntity;

import lombok.*;



@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuedVc extends BaseEntity {

  private String credIdStored;
  private String presExId;
  private String verifyInviUrl;
  private Long bookingId;
  private Long userId;
  private Long tenantId;
  private VcStatus status;

  public void activate(String credIdStored) {
    this.credIdStored = credIdStored;
    status = VcStatus.ISSUED;
  }

  public enum VcStatus {
    PENDING, ISSUED, CONSUMED, REVOKED
  }

  public void verified() {
    this.status = VcStatus.CONSUMED;
  }

  /**
   * 현재 상태가 ISSUED인지 확인하는 메서드
   */
  public boolean isIssued() {
    return VcStatus.ISSUED.equals(status);
  }
}
