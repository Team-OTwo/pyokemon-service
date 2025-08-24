package com.pyokemon.did.api.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ConnectionWebhookDto {

  private String state;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  @JsonProperty("connection_id")
  private String connectionId;

  @JsonProperty("their_role")
  private String theirRole;

  @JsonProperty("connection_protocol")
  private String connectionProtocol;

  @JsonProperty("rfc23_state")
  private String rfc23State;

  @JsonProperty("invitation_key")
  private String invitationKey;

  @JsonProperty("invitation_msg_id")
  private String invitationMsgId;

  private String accept;

  @JsonProperty("invitation_mode")
  private String invitationMode;

  private String alias;

  @JsonProperty("their_did")
  private String theirDid;

  @JsonProperty("their_label")
  private String theirLabel;

  @JsonProperty("request_id")
  private String requestId;

  @JsonProperty("my_did")
  private String myDid;
}