package com.pyokemon.did.remote.acapy.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtVerifyResponse {

  private boolean valid;
  private JwtPayload payload;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class JwtPayload {
    private String did;
  }
}
