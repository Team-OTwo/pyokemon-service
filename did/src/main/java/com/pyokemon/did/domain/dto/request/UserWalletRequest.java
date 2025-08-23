package com.pyokemon.did.domain.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

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
