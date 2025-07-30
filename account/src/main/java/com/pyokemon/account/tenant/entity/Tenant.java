package com.pyokemon.account.tenant.entity;

import java.time.LocalDateTime;

import com.pyokemon.account.tenant.dto.request.UpdateTenantProfileRequestDto;
import com.pyokemon.account.tenant.dto.response.TenantProfileResponseDto;

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
public class Tenant {

  private Long tenantId;
  private Long accountId;
  private String name;
  private String corpId;
  private String city;
  private String street;
  private String zipcode;
  private String ceo;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public TenantProfileResponseDto toTenantProfileResponseDto(String loginId) {
    return TenantProfileResponseDto.builder().tenantId(this.tenantId).accountId(this.accountId)
        .loginId(loginId).name(this.name).corpId(this.corpId).city(this.city).street(this.street)
        .zipcode(this.zipcode).ceo(this.ceo).createdAt(this.createdAt).updatedAt(this.updatedAt)
        .build();
  }

  public Tenant updateFromRequest(UpdateTenantProfileRequestDto request) {
    return Tenant.builder().tenantId(this.tenantId).accountId(this.accountId).name(this.name) // 기존
                                                                                              // 값
                                                                                              // 유지
        .corpId(this.corpId) // 기존 값 유지
        .city(request.getCity()).street(request.getStreet()).zipcode(request.getZipcode())
        .ceo(request.getCeo()).build();
  }
}
