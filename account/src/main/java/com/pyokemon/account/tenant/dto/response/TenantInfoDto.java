package com.pyokemon.account.tenant.dto.response;

import com.pyokemon.account.tenant.entity.Tenant;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantInfoDto {

  private Long tenantId;
  private String name;

  public static TenantInfoDto from(Tenant tenant) {
    return TenantInfoDto.builder().tenantId(tenant.getTenantId()).name(tenant.getName()).build();
  }
}
