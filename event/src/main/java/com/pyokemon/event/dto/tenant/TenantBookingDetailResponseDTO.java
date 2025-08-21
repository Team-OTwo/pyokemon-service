package com.pyokemon.event.dto.tenant;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantBookingDetailResponseDTO {
  private Long eventId;
  private String title;
  private String genre;
  private String status;
  private Long eventScheduleId;
  private String ticketOpenAt;
  private String eventDate;
  private String venueName;
  private List<BookingStatusInfo> bookingStatus;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BookingStatusInfo {
    private Long seatClassId;
    private String className;
    private Integer totalSeats;
    private Integer bookedSeats;
    private Integer availableSeats;
    private Integer price;
    private Double bookingRate;
  }
}
