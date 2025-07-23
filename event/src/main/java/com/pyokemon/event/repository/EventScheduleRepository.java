package com.pyokemon.event.repository;

import com.pyokemon.event.entity.EventSchedule;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventScheduleRepository {
    List<EventSchedule> selectTodayOpenedTickets();
}
