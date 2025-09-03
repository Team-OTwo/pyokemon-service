package com.pyokemon.did.domain.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class VerificationRequest {

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CreateVerificationRequest {

    @NotNull
    @JsonProperty("booking_id")
    private Long bookingId;

    @NotNull
    private String jwt;
  }

}
