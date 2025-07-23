package com.pyokemon.tenant.api.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantListResponseDto {

  private List<TenantDetailResponseDto> tenants;
  private int totalCount;

  // 편의 생성자
  public static TenantListResponseDto of(List<TenantDetailResponseDto> tenants) {
    return TenantListResponseDto.builder().tenants(tenants).totalCount(tenants.size()).build();
  }
}
