package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet extends BaseEntity {
  private Long accountId;
  private AccountRole accountRole;
  private String token;
  private String publicDid;
  private String publicVerKey;

  public enum AccountRole {
    USER, TENANT
  }
}
