package com.pyokemon.tenant.mapper;

import com.pyokemon.tenant.api.dto.request.CreateTenantRequestDto;
import com.pyokemon.tenant.api.dto.response.TenantDetailResponseDto;
import com.pyokemon.tenant.api.dto.response.TenantListResponseDto;
import com.pyokemon.tenant.api.entity.Tenant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TenantMapper {

    /**
     * Tenant 엔티티를 TenantDetailResponseDto로 변환
     */
    public TenantDetailResponseDto toResponseDto(Tenant tenant) {
        if (tenant == null) {
            return null;
        }

        return TenantDetailResponseDto.builder()
                .id(tenant.getId())
                .loginId(tenant.getLoginId())
                .corpName(tenant.getCorpName())
                .corpId(tenant.getCorpId())
                .city(tenant.getCity())
                .street(tenant.getStreet())
                .zipcode(tenant.getZipcode())
                .ceoName(tenant.getCeoName())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }

    /**
     * Tenant 리스트를 TenantListResponseDto로 변환
     */
    public TenantListResponseDto toListResponseDto(List<Tenant> tenants) {
        if (tenants == null || tenants.isEmpty()) {
            return TenantListResponseDto.of(List.of());
        }

        List<TenantDetailResponseDto> tenantDtos = tenants.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        return TenantListResponseDto.of(tenantDtos);
    }

    /**
     * CreateTenantRequestDto를 Tenant 엔티티로 변환
     */
    public Tenant toEntity(CreateTenantRequestDto request, String encodedPassword) {
        if (request == null) {
            return null;
        }

        return Tenant.builder()
                .loginId(request.getLoginId())
                .password(encodedPassword)
                .corpName(request.getCorpName())
                .corpId(request.getBusinessNumber())
                .city(parseCity(request.getCity()))
                .street(parseStreet(request.getStreet()))
                .zipcode(parseZipcode(request.getZipcode()))
                .ceoName(request.getCeoName())
                .build();
    }

    /**
     * 주소에서 도시 추출
     */
    private String parseCity(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }
        // 예: "서울시 강남구 테헤란로 123" -> "서울시 강남구"
        String[] parts = address.trim().split(" ");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return parts.length > 0 ? parts[0] : "";
    }

    /**
     * 주소에서 도로명 추출
     */
    private String parseStreet(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }
        // 예: "서울시 강남구 테헤란로 123" -> "테헤란로 123"
        String[] parts = address.trim().split(" ");
        if (parts.length >= 3) {
            return String.join(" ", java.util.Arrays.copyOfRange(parts, 2, parts.length));
        }
        return "";
    }

    /**
     * 주소에서 우편번호 추출 (현재는 빈 문자열 반환)
     * 실제로는 우편번호 API나 별도 필드로 받아야 함
     */
    private String parseZipcode(String address) {
        // TODO: 우편번호는 별도로 받거나 API로 조회 필요
        return "";
    }
} 