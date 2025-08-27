package com.pyokemon.did.remote.acapy.common.dto.request.presentproof;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 증명 제시 옵션
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Options {
    @JsonProperty("challenge")
    private String challenge;
    
    @JsonProperty("domain")
    private String domain;
}
