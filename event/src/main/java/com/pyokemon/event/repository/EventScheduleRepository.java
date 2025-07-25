package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.entity.EventSchedule;

@Mapper
public interface EventScheduleRepository {
  List<EventItemResponseDTO> selectTodayOpenedTickets();

  List<EventItemResponseDTO> selectTicketsToBeOpened();

  Long save(EventSchedule eventSchedule);
}
