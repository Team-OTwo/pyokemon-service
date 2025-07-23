package com.pyokemon.tenant.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDetailResponseDto {

    private Long id;
    private String loginId;
    private String corpName;       // 회사명
    private String corpId;         // 사업자번호
    private String city;           // 도시
    private String street;         // 도로명
    private String zipcode;        // 우편번호
    private String ceoName;        // 대표자명
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 주소를 하나의 문자열로 반환하는 편의 메서드
    public String getFullAddress() {
        return String.format("(%s) %s %s", zipcode, city, street);
    }
}

