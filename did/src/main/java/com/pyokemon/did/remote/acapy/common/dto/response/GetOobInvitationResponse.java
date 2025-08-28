package com.pyokemon.did.remote.acapy.common.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py OOB 초대장 조회 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOobInvitationResponse implements AcaPyResponse {

  @JsonProperty("oob_id")
  private String oobId;

  private Invitation invitation;

  @JsonProperty("invitation_url")
  private String invitationUrl;

  private String state;
}
