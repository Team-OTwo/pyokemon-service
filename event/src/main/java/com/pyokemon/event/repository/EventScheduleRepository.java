package com.pyokemon.event.repository;

import com.pyokemon.event.entity.EventSchedule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EventScheduleRepository {
    List<EventSchedule> selectTodayOpenedTickets();
    List<EventSchedule> selectTicketsToBeOpened();
}
