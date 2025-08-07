package com.pyokemon.event.dto;

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
    // event 정보
    private Long eventId;
    private String title;
    private Long ageLimit;
    private String description;
    private String genre;
    private String thumbnailUrl;
    private String status;

    // event schedule 정보
    private Long eventScheduleId;
    private LocalDateTime ticketOpenAt;
    private LocalDateTime eventDate;

    // venue 정보
    private String venueName;

    // 가격 정보
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