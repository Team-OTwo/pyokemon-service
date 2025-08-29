package com.pyokemon.did.remote.acapy.common.dto.request.presentproof;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 증명 제시 필터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
  @JsonProperty("type")
  private String type;

  @JsonProperty("const")
  private String constant;
}
