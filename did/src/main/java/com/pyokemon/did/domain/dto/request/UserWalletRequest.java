package com.pyokemon.did.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

public class UserWalletRequest {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserWalletRequest {
        @JsonProperty("user_id")
        private Long userId;
    }
}
