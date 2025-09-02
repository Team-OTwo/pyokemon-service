package com.pyokemon.account.tenant.entity;

import com.pyokemon.account.tenant.dto.request.UpdateTenantProfileRequestDto;
import com.pyokemon.account.tenant.dto.response.TenantProfileResponseDto;
import com.pyokemon.common.entity.BaseEntity;

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
public class Tenant extends BaseEntity {

  private Long accountId;
  private String name;
  private String corpId;
  private String city;
  private String street;
  private String zipcode;
  private String ceo;


  public TenantProfileResponseDto to(String loginId) {
    return TenantProfileResponseDto.builder().accountId(this.accountId).loginId(loginId)
        .name(this.name).corpId(this.corpId).city(this.city).street(this.street)
        .zipcode(this.zipcode).ceo(this.ceo).build();
  }

  public Tenant update(UpdateTenantProfileRequestDto request) {
    return Tenant.builder().accountId(this.accountId).name(this.name) // 기존
        .corpId(this.corpId) // 기존 값 유지
        .city(request.getCity()).street(request.getStreet()).zipcode(request.getZipcode())
        .ceo(request.getCeo()).build();
  }
}
