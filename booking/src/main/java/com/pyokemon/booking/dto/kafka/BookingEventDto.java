package com.pyokemon.booking.dto.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEventDto {

  @JsonProperty("booking_id")
  private Long bookingId;

  @JsonProperty("event_schedule_id")
  private Long eventScheduleId;

  @JsonProperty("account_id")
  private Long accountId;

  @JsonProperty("status")
  private String status;

}
