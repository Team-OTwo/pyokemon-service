package com.pyokemon.did.domain.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class VerificationResponse {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateVerificationResponse {
    private String VerifyInviUrl;
    private String presExId;
  }


  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class HandleVerificationResponse{

    @NotNull
    private String status;
  }

}
