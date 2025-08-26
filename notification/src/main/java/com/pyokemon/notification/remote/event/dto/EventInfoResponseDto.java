package com.pyokemon.notification.remote.event.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInfoResponseDto {

    // 응답 데이터
    private EventInfo event;
    private EventScheduleInfo event_schedule;
    private SeatInfo seat;

    @Getter
    public static class EventInfo {
        private Long event_id;
        private String event_title;
    }

    @Getter
    public static class EventScheduleInfo {
        private String ticket_open_at;
        private String event_date;
    }

    @Getter
    public static class SeatInfo {
        private String floor;
        private String row;
        private String col;
        private SeatClassInfo seat_class;
    }

    @Getter
    public static class SeatClassInfo {
        private Long seat_class_id;
        private String class_name;
    }

}
