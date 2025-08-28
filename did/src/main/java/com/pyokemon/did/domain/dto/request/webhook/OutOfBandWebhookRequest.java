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

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  @JsonProperty("oob_id")
  private String oobId;

  @JsonProperty("invi_msg_id")
  private String inviMsgId;

  private Invitation invitation;

  @JsonProperty("connection_id")
  private String connectionId;

  @JsonProperty("our_recipient_key")
  private String ourRecipientKey;

  private String role;

  @JsonProperty("multi_use")
  private boolean multiUse;
}
