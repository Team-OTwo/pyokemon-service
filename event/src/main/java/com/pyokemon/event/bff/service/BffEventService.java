package com.pyokemon.event.bff.service;

import com.pyokemon.event.bff.dto.*;
import com.pyokemon.event.bff.repository.BffEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BffEventService {
    private final BffEventRepository repo;

    public BffEventScheduleDto getEventSchedule(Long id) {
        return repo.findEventScheduleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EVENT_SCHEDULE_NOT_FOUND"));
    }
    public BffVenueDto getVenue(Long id) {
        return repo.findVenueById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "VENUE_NOT_FOUND"));
    }
    public BffSeatDto getSeat(Long id) {
        return repo.findSeatById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SEAT_NOT_FOUND"));
    }
    public BffEventDto getEvent(Long id) {
        return repo.findEventById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "EVENT_NOT_FOUND"));
    }
    public BffSeatClassDto getSeatClass(Long id) {
        return repo.findSeatClassById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SEAT_CLASS_NOT_FOUND"));
    }
}

