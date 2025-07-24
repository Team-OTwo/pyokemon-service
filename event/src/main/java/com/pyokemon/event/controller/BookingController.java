package com.pyokemon.event.controller;

import com.pyokemon.event.dto.EventScheduleSeatResponse;
import com.pyokemon.event.dto.SeatMapDetail;
import com.pyokemon.event.service.BookingSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class BookingController {
    private final BookingSeatService bookingSeatService;

    @GetMapping("/schedules/{eventScheduleId}")
    public ResponseEntity<EventScheduleSeatResponse> getEventScheduleSeats(@PathVariable Long eventScheduleId) {
        EventScheduleSeatResponse response = bookingSeatService.getEventScheduleSeats(eventScheduleId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/schedules/{eventScheduleId}/seats")
    public ResponseEntity<List<SeatMapDetail>> getSeatMapOnly(@PathVariable Long eventScheduleId) {
        List<SeatMapDetail> seatMap = bookingSeatService.getSeatMapOnly(eventScheduleId);
        if (seatMap == null || seatMap.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(seatMap);
    }
}