package com.pyokemon.did.api.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OutOfBandWebhookDto {

  private String state;

  @JsonProperty("created_at")
  private String createdAt;

  @JsonProperty("updated_at")
  private String updatedAt;

  private boolean trace;

  @JsonProperty("oob_id")
  private String oobId;

  @JsonProperty("invi_msg_id")
  private String inviMsgId;

  private InvitationDto invitation;

  @JsonProperty("connection_id")
  private String connectionId;

  @JsonProperty("our_recipient_key")
  private String ourRecipientKey;

  private String role;

  @JsonProperty("multi_use")
  private boolean multiUse;


  @Data
  public static class InvitationDto {

    @JsonProperty("@type")
    private String type;

    @JsonProperty("@id")
    private String id;

    private String label;

    @JsonProperty("handshake_protocols")
    private String[] handshakeProtocols;

    private String[] accept;

    private String[] services;
  }
}
