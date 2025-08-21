package com.pyokemon.event.dto.tenant;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantEventDetailResponseDTO {
  private Long eventId;
  private String title;
  private Long ageLimit;
  private String description;
  private String genre;
  private String thumbnailUrl;
  private String status;
  private Long eventScheduleId;
  private LocalDateTime ticketOpenAt;
  private LocalDateTime eventDate;
  private String venueName;
  private List<PriceInfo> prices;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PriceInfo {
    private Long priceId;
    private Long seatClassId;
    private String className;
    private Integer price;
  }
}
