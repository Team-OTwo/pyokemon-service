package com.pyokemon.did.domain;

import org.springframework.data.annotation.Id;

import com.pyokemon.common.entity.BaseEntity;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantWallet extends BaseEntity {
  private Long tenantId;
  private String token;
  private String publicDid;
  private String publicVerkey;
}
