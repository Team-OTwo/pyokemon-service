package com.pyokemon.did.domain.dto.request.webhook;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BasicMessageWebhookRequest {

  @JsonProperty("content")
  private String content;

  @JsonProperty("state")
  private String state;


  @JsonProperty("connection_id")
  private String connectionId;

  @JsonProperty("message_id")
  private String messageId;

  @JsonProperty("sent_time")
  private LocalDateTime sentTime;

}
