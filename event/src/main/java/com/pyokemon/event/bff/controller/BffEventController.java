package com.pyokemon.event.bff.controller;

import com.pyokemon.common.dto.IdsRequest;
import com.pyokemon.event.bff.dto.*;
import com.pyokemon.event.bff.service.BffEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BffEventController {

    private final BffEventService bffEventService;

    // Event Schedule 엔드포인트
    @GetMapping("/event-schedules/{eventScheduleId}")
    public BffEventScheduleDto getEventSchedule(@PathVariable Long eventScheduleId) {
        return bffEventService.getEventSchedule(eventScheduleId);
    }

    // Venue 엔드포인트
    @GetMapping("/venues/{venueId}")
    public BffVenueDto getVenue(@PathVariable Long venueId) {
        return bffEventService.getVenue(venueId);
    }

    // Seat 엔드포인트
    @GetMapping("/seats/{seatId}")
    public BffSeatDto getSeat(@PathVariable Long seatId) {
        return bffEventService.getSeat(seatId);
    }

    // Event 엔드포인트
    @GetMapping("/bff/events/{eventId}")
    public BffEventDto getEvent(@PathVariable Long eventId) {
        return bffEventService.getEvent(eventId);
    }

    // Seat Class 엔드포인트
    @GetMapping("/seat-classes/{seatClassId}")
    public BffSeatClassDto getSeatClass(@PathVariable Long seatClassId) {
        return bffEventService.getSeatClass(seatClassId);
    }

    @PostMapping("/seats/_batch")
    public List<BffSeatDto> getSeat(@RequestBody IdsRequest request) {
        return bffEventService.getSeats(request.getIds());
    }

    @PostMapping("/seat-classes/_batch")
    public List<BffSeatClassDto> getSeatClasses(@RequestBody IdsRequest request) {
        return bffEventService.getSeatClasses(request.getIds());
    }

    @PostMapping("/event-schedules/_batch")
    public List<BffEventScheduleDto> getEventSchedules(@RequestBody IdsRequest request){
        return bffEventService.getEventSchedules(request.getIds());
    }

    @PostMapping("/bff/events/_batch")
    public List<BffEventDto> getEvents(@RequestBody IdsRequest request){
        System.out.println(request.getIds().get(0));
        return bffEventService.getEvents(request.getIds());
    }

    @PostMapping("/venues/_batch")
    public List<BffVenueDto> getVenues(@RequestBody IdsRequest request){
        return bffEventService.getVenues(request.getIds());
    }


}







