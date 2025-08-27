package com.pyokemon.did.remote.acapy.common.dto.request.presentproof;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 증명 제시 정의
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresentationDefinition {
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("input_descriptors")
    private List<InputDescriptor> inputDescriptors;
}
