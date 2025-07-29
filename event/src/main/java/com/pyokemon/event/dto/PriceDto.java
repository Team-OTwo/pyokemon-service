package com.pyokemon.event.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

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
public class PriceDto {

  private Long priceId;

  private Long eventScheduleId;

  @NotNull(message = "Seat class ID is required")
  private Long seatClassId;

  @NotNull(message = "Price is required")
  @Min(value = 0, message = "Price cannot be negative")
  private Integer price;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

}
