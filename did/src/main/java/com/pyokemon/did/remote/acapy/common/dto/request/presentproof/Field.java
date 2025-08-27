package com.pyokemon.did.remote.acapy.common.dto.request.presentproof;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 증명 제시 필드
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Field {
    @JsonProperty("path")
    private List<String> path;
    
    @JsonProperty("filter")
    private Filter filter;
}
