package com.pyokemon.account.tenant.bff.dto;

import com.pyokemon.account.tenant.entity.Tenant;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDto {

  private Long id;
  private String name;

  public static TenantDto from(Tenant tenant) {
    return TenantDto.builder().id(tenant.getId()).name(tenant.getName()).build();
  }
}
