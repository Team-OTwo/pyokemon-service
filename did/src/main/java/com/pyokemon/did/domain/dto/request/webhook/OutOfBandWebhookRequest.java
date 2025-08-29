package com.pyokemon.did.domain.dto.request.webhook;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.remote.acapy.common.dto.response.Invitation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutOfBandWebhookRequest {

  private String state;

  private String role;

  @JsonProperty("oob_id")
  private String oobId;

  @JsonProperty("invi_msg_id")
  private String inviMsgId;

  @JsonProperty("connection_id")
  private String connectionId;

}
