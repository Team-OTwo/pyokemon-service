package com.pyokemon.account.tenant.dto.response;

import com.pyokemon.account.tenant.entity.Tenant;

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
public class TenantProfileResponseDto {

  private Long accountId;
  private String loginId;
  private String name;
  private String corpId;
  private String city;
  private String street;
  private String zipcode;
  private String ceo;

  public static TenantProfileResponseDto of(Tenant tenant, String loginId) {
    return TenantProfileResponseDto.builder().accountId(tenant.getAccountId()).loginId(loginId)
        .name(tenant.getName()).corpId(tenant.getCorpId()).city(tenant.getCity())
        .street(tenant.getStreet()).zipcode(tenant.getZipcode()).ceo(tenant.getCeo()).build();
  }

}
