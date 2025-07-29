package com.pyokemon.tenant.api.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TenantListResponseDto {

  private final List<TenantDetailResponseDto> tenants;
  private final int totalCount;

  // 편의 생성자
  public static TenantListResponseDto of(List<TenantDetailResponseDto> tenants) {
    return TenantListResponseDto.builder().tenants(tenants).totalCount(tenants.size()).build();
  }
}
