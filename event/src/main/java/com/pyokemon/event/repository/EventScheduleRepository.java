package com.pyokemon.event.repository;


import com.pyokemon.event.entity.EventSchedule;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Optional;

import com.pyokemon.event.dto.EventItemResponseDTO;
@Mapper
public interface EventScheduleRepository {
    
  List<EventItemResponseDTO> selectTodayOpenedTickets();
  List<EventItemResponseDTO> selectTicketsToBeOpened();
  Optional<EventSchedule> findById(Long id);
  Long save(EventSchedule eventSchedule);

}
