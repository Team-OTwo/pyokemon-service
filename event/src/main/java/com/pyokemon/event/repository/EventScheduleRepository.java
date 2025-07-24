package com.pyokemon.event.repository;

import com.pyokemon.event.dto.EventItemResponseDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EventScheduleRepository {
    List<EventItemResponseDTO> selectTodayOpenedTickets();
    List<EventItemResponseDTO> selectTicketsToBeOpened();
}
