package com.pyokemon.did.remote.acapy.common.dto.request.presentproof;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
}
