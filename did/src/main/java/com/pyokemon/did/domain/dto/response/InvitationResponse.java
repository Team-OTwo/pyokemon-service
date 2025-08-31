package com.pyokemon.did.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {

  @JsonProperty("mediator_acapy_invi_url")
  private String mediatorAcaPyInviUrl;

  @JsonProperty("user_acapy_invi_url")
  private String userAcaPyInviUrl;


}
