package com.pyokemon.tenant.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.pyokemon.tenant.api.dto.request.CreateTenantRequestDto;
import com.pyokemon.tenant.api.dto.response.TenantDetailResponseDto;
import com.pyokemon.tenant.api.dto.response.TenantListResponseDto;
import com.pyokemon.tenant.api.entity.Tenant;

@Component
public class TenantConverter {

  // Tenant 엔티티를 TenantDetailResponseDto로 변환
  public TenantDetailResponseDto toResponseDto(Tenant tenant) {
    if (tenant == null) {
      return null;
    }

    return TenantDetailResponseDto.builder().id(tenant.getId()).loginId(tenant.getLoginId())
        .corpName(tenant.getCorpName()).corpId(tenant.getCorpId()).city(tenant.getCity())
        .street(tenant.getStreet()).zipcode(tenant.getZipcode()).ceoName(tenant.getCeoName())
        .createdAt(tenant.getCreatedAt()).updatedAt(tenant.getUpdatedAt()).build();
  }

  // Tenant 리스트를 TenantListResponseDto로 변환
  public TenantListResponseDto toListResponseDto(List<Tenant> tenants) {
    if (tenants == null || tenants.isEmpty()) {
      return TenantListResponseDto.of(List.of());
    }

    List<TenantDetailResponseDto> tenantDtos =
        tenants.stream().map(this::toResponseDto).collect(Collectors.toList());

    return TenantListResponseDto.of(tenantDtos);
  }

  // CreateTenantRequestDto를 Tenant 엔티티로 변환
  public Tenant toEntity(CreateTenantRequestDto request, String encodedPassword) {
    if (request == null) {
      return null;
    }

    return Tenant.builder().loginId(request.getLoginId()).password(encodedPassword)
        .corpName(request.getCorpName()).corpId(request.getBusinessNumber()).city(request.getCity())
        .street(request.getStreet()).zipcode(request.getZipcode()).ceoName(request.getCeoName())
        .build();
  }

}
