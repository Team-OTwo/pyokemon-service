package com.pyokemon.did.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class TenantInvitationResponse {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TenantInvitation {
        private Long tenantId;
        private String invitationUrl;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateTenantInvitationResponse {
        private List<TenantInvitation> invitations;
    }
}
