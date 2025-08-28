package com.pyokemon.did.remote.acapy.common.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.dto.base.AcaPyRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ACA-Py에 OOB 초대장 조회를 요청하기 위한 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetOobInvitationRequest implements AcaPyRequest {

  @JsonProperty("invitation_id")
  private String invitationId;
}
