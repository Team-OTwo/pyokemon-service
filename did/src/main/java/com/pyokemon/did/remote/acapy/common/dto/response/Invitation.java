package com.pyokemon.did.remote.acapy.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ACA-Py 초대장 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    
    @JsonProperty("@type")
    private String type;
    
    @JsonProperty("@id")
    private String id;
    
    private String label;
    
    @JsonProperty("handshake_protocols")
    private List<String> handshakeProtocols;
    
    private List<String> services;
}
