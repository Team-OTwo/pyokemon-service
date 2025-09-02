package com.pyokemon.did.domain.dto.request.webhook;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LdProofWebhookRequest {

  @NotNull
  @JsonProperty("created_at")
  private String createdAt;

  @NotNull
  @JsonProperty("updated_at")
  private String updatedAt;

  @NotNull
  @JsonProperty("cred_ex_ld_proof_id")
  private String credExLdProofId;

  @NotNull
  @JsonProperty("cred_ex_id")
  private String credExId;

  @NotNull
  @JsonProperty("cred_id_stored")
  private String credIdStored;
}
