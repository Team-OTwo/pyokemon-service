package com.pyokemon.did.remote.acapy.common.dto.request.presentproof;

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
}
