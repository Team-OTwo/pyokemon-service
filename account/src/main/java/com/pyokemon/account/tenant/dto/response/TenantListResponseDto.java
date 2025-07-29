package com.pyokemon.account.tenant.dto.response;

import java.time.LocalDateTime;
import java.util.List;

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
public class TenantListResponseDto {

  private List<TenantSummaryDto> tenants;
  private int totalCount;

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TenantSummaryDto {
    private Long tenantId;
    private String loginId;
    private String name;
    private String corpId;
    private String city;
    private LocalDateTime createdAt;
  }
}
