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

  @JsonProperty("connection_id")
  private String connectionId;

  @JsonProperty("message_id")
  private String messageId;

  @JsonProperty("content")
  private String content;

  @JsonProperty("state")
  private String state;

  @JsonProperty("sent_time")
  private LocalDateTime sentTime;

  // "created_at", "updated_at", "trace" 필드는 필요하다면 추가할 수 있습니다.

}
