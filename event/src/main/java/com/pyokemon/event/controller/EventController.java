package com.pyokemon.event.controller;


import com.pyokemon.event.entity.EventSchedule;
import com.pyokemon.event.service.EventScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event")
@RequiredArgsConstructor
public class EventController {
    @Autowired
    private EventScheduleService eventScheduleService;

    //오늘 오픈 티켓
    @GetMapping("/open-today")
    public List<EventSchedule> getOpenTicketsToday() {
        return eventScheduleService.getTodayOpenedTickets();
    }

    //오픈 예정 티켓
    @GetMapping("/tobeopend")
    public List<EventSchedule> getOpenTicketsToBeOpened() {
        return eventScheduleService.getTicketsToBeOpened();
    }


}
