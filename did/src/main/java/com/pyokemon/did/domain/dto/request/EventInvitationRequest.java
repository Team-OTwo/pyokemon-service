package com.pyokemon.did.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
