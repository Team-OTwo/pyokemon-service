package com.pyokemon.did.domain.dto.request;


import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이벤트 초대장 관련 요청 DTO 그룹
 */
public class EventInvitationRequest {

  /**
   * 이벤트 초대장 프로비저닝 요청 DTO
   */
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateEventInvitationRequest {
    @NotNull
    private Long tenantId;

    @NotNull
    private Long eventId;
  }
}
