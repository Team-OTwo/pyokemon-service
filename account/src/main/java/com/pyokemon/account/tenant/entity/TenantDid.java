package com.pyokemon.account.tenant.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDid {

  private Long tenantDidId;
  private Long tenantId;
  private String did;
  private Boolean isValid;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
