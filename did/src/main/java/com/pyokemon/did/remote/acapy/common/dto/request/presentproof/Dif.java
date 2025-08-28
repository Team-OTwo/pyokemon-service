package com.pyokemon.did.remote.acapy.common.dto.request.presentproof;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DIF(Decentralized Identity Foundation) 증명 제시
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dif {
  @JsonProperty("options")
  private Options options;

  @JsonProperty("presentation_definition")
  private PresentationDefinition presentationDefinition;

  /**
   * 증명 제시 옵션
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Options {
    @JsonProperty("challenge")
    private String challenge;

    @JsonProperty("domain")
    private String domain;
  }

  /**
   * 증명 제시 정의
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PresentationDefinition {
    @JsonProperty("id")
    private String id;

    @JsonProperty("input_descriptors")
    private List<InputDescriptor> inputDescriptors;
  }
}
