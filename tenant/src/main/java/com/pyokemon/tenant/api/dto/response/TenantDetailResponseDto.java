package com.pyokemon.tenant.api.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TenantDetailResponseDto {

  private final Long id;
  private final String loginId;
  private final String corpName; // 회사명
  private final String corpId; // 사업자번호
  private final String city; // 도시
  private final String street; // 도로명
  private final String zipcode; // 우편번호
  private final String ceoName; // 대표자명
  private final LocalDateTime createdAt;
  private final LocalDateTime updatedAt;

  // 주소를 하나의 문자열로 반환하는 편의 메서드
  public String getFullAddress() {
    return String.format("(%s) %s %s", zipcode, city, street);
  }
}
