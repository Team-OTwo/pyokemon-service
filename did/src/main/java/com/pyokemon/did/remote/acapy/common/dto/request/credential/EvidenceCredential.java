package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * 증거(Evidence)가 포함된 자격 증명
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EvidenceCredential extends BaseCredential {
    
    @JsonProperty("evidence")
    private List<Evidence> evidence;
}
