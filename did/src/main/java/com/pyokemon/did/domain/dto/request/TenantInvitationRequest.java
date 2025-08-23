package com.pyokemon.did.domain.dto.request;


import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이벤트 초대장 관련 요청 DTO 그룹
 */
public class TenantInvitationRequest {

  /**
   * 이벤트 초대장 프로비저닝 요청 DTO
   */
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateTenantInvitationRequest {
    @NotNull
    private Long userId;

    @NotNull
    private String deviceId;

    private List<Long> tenantIds;
  }
}
