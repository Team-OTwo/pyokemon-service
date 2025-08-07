package com.pyokemon.event.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
public class EventRegisterDto {

  private Long eventId;

  // @NotNull(message = "Account ID is required") // 임시로 주석 처리 (account 서비스 완료 전까지)
  private Long accountId;

  @NotBlank(message = "Title is required")
  @Size(max = 100, message = "Title must be less than 100 characters")
  private String title;

  @NotNull(message = "Age limit is required")
  @Min(value = 0, message = "Age limit cannot be negative")
  private Long ageLimit;

  @NotBlank(message = "Description is required")
  private String description;

  @NotBlank(message = "Genre is required")
  private String genre;

  private String thumbnailUrl;

  private EventStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Valid
  private List<EventScheduleDto> schedules;
}
