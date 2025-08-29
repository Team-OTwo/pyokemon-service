package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;

import lombok.*;



@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuedVc extends BaseEntity {

  private String credExId;
  private String presExId;
  private String verifyInviUrl;
  private Long bookingId;
  private Long userId;
  private Long tenantId;
  private VcStatus status;

  public enum VcStatus {
    ISSUED, CONSUMED, REVOKED
  }
}
