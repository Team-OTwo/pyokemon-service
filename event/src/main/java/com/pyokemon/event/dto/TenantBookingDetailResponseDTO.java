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
public class TenantBookingDetailResponseDTO {
    // event 정보
    private Long eventId;
    private String title;
    private String genre;
    private String status;

    // event schedule 정보
    private Long eventScheduleId;
    private LocalDateTime ticketOpenAt;
    private LocalDateTime eventDate;

    // venue 정보
    private String venueName;

    // 예매 현황 정보
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
        private Double bookingRate; // 예매율 (예매된 좌석 / 전체 좌석 * 100)
    }
} 