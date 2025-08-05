package com.pyokemon.event.dto;

import java.time.LocalDateTime;

import com.pyokemon.event.entity.Event.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantEventListDto {
  private Long eventId;
  private String thumbnailUrl;
  private String title;
  private LocalDateTime eventDate;
  private String venueName;
  private EventStatus status;
} 