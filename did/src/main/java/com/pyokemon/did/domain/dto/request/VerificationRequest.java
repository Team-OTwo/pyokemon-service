package com.pyokemon.did.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class VerificationRequest {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateVerificationRequest {
        @NotNull
        private Long booking_id;

        @NotNull
        private String jwt;
    }

}
