package com.pyokemon.did.remote.acapy.common.dto.response;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py 공개 DID 생성 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePublicDidResponse implements AcaPyResponse {

  private Result result;


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Result {
    @NotNull
    @JsonProperty("did")
    private String did;

    @NotNull
    @JsonProperty("verkey")
    private String verkey;
  }
}
