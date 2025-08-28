package com.pyokemon.event.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelEventResponseDTO {
  private Long eventId;
  private String status;
}
