package com.pyokemon.did.remote.acapy.common.dto.request.presentproof;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 증명 제시 입력 디스크립터
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InputDescriptor {
  @JsonProperty("id")
  private String id;

  @JsonProperty("schema")
  private List<Schema> schema;

  @JsonProperty("constraints")
  private Constraints constraints;
}
