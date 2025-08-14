package com.pyokemon.event.controller;

import com.pyokemon.event.dto.bff.BffVenueDto;
import com.pyokemon.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final EventService eventService;

    // bff
    @GetMapping("/bff/{venueId}")
    public BffVenueDto getBffVenue(@PathVariable Long venueId){
        return eventService.getBffVenue(venueId);
    }
}
