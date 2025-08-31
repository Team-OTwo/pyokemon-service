package com.pyokemon.did.domain.dto.request;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DelegateCredentialRequest {
  @NotNull
  @JsonProperty("booking_id")
  private Long bookingId;
}
