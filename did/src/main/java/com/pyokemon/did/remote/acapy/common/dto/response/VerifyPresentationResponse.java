package com.pyokemon.did.remote.acapy.common.dto.response;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPresentationResponse {
  @NotNull
  private String state;

  @NotNull
  private boolean verified;

  public boolean verify() {
    return state.equals("done") && verified;
  }
}
