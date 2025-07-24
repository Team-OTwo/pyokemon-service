package com.pyokemon.event.controller;

import com.pyokemon.event.dto.EventScheduleSeatResponse;
import com.pyokemon.event.service.BookingSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookingSeatController {
    private final BookingSeatService bookingSeatService;

    @GetMapping("/schedules/{eventScheduleId}/seats")
    public ResponseEntity<EventScheduleSeatResponse> getEventScheduleSeats(@PathVariable Long eventScheduleId) {
        EventScheduleSeatResponse response = bookingSeatService.getEventScheduleSeats(eventScheduleId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
}

