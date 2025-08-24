package com.pyokemon.did.domain.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class EventInvitationRequest {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateEventInvitationRequest {
    @NotNull
    private Long userId;

    @NotNull
    private String deviceId;

    private List<Long> tenantIds;
  }
}
