package com.pyokemon.did.remote.acapy.common.dto.request.presentproof;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 증명 제시 제약 조건
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Constraints {
  @JsonProperty("fields")
  private List<Field> fields;

  /**
   * 증명 제시 필드
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Field {
    @JsonProperty("path")
    private List<String> path;

    @JsonProperty("filter")
    private Filter filter;
  }

}
