package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDetailResponseDTO {
    // 요청 데이터
    private Long event_schedule_id;
    private Long seat_id;
    
    // 응답 데이터
    private EventInfo event;
    private EventScheduleInfo event_schedule;
    private SeatInfo seat;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventInfo {
        private Long event_id;
        private String event_title;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventScheduleInfo {
        private String ticket_open_at;
        private String event_date;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatInfo {
        private String floor;
        private String row;
        private String col;
        private SeatClassInfo seat_class;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatClassInfo {
        private Long seat_class_id;
        private String class_name;
    }
}
