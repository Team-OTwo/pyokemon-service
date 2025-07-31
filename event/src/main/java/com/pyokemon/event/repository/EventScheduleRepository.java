package com.pyokemon.event.repository;


import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.entity.EventSchedule;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EventScheduleRepository {

  List<EventItemResponseDTO> selectTodayOpenedTickets();

  List<EventItemResponseDTO> selectTicketsToBeOpened();

  Optional<EventSchedule> findById(Long id);

  Long save(EventSchedule eventSchedule);

  List<EventItemResponseDTO> selectEventList(@Param("genre") String genre, @Param("limit") int limit, @Param("offset") int offset);

  int getTotalCountByGenre(@Param("genre") String genre);


}
