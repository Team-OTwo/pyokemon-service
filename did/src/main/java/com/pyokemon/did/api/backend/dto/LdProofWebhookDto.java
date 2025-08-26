package com.pyokemon.did.api.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LdProofWebhookDto {
    
    @JsonProperty("created_at")
    private String createdAt;
    
    @JsonProperty("updated_at")
    private String updatedAt;
    
    @JsonProperty("cred_ex_ld_proof_id")
    private String credExLdProofId;
    
    @JsonProperty("cred_ex_id")
    private String credExId;
    
    @JsonProperty("cred_id_stored")
    private String credIdStored;
}
