package com.pyokemon.event.controller;

import com.pyokemon.event.dto.EventScheduleSeatResponse;
import com.pyokemon.event.service.BookingSeatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event/bookings") // 예약 관련 주소임을 명확히 함
public class BookingSeatController {

    private final BookingSeatService bookingSeatService; // 서비스 인스턴스 이름도 변경

    public BookingSeatController(BookingSeatService bookingSeatService) {
        this.bookingSeatService = bookingSeatService;
    }

    /**
     * 특정 공연 일정의 좌석 배치도 및 잔여 좌석 정보를 조회합니다.
     * GET /api/bookings/schedules/{eventScheduleId}/seats
     * @param eventScheduleId 조회하려는 공연 일정 ID
     * @return 공연 일정 좌석 정보 (EventScheduleSeatResponse DTO)
     */
    @GetMapping("/schedules/{eventScheduleId}/seats")
    public ResponseEntity<EventScheduleSeatResponse> getEventScheduleSeats(@PathVariable Long eventScheduleId) {
        // Service 계층 호출
        EventScheduleSeatResponse response = bookingSeatService.getEventScheduleSeats(eventScheduleId);

        // 결과가 null이거나 유효하지 않으면 404 Not Found 반환 가능
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}
