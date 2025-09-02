package com.pyokemon.account.tenant.dto.response;

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
    private String loginId;
    private String name;
    private String corpId;
    private String city;

    public static TenantSummaryDto fromTenant(com.pyokemon.account.tenant.entity.Tenant tenant,
        String loginId) {
      return TenantSummaryDto.builder().loginId(loginId != null ? loginId : "N/A")
          .name(tenant.getName()).corpId(tenant.getCorpId()).city(tenant.getCity()).build();
    }
  }
}
