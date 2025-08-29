package com.pyokemon.event.repository;


import java.util.List;
import java.util.Optional;

import com.pyokemon.event.dto.EventScheduleInfoDto;
import com.pyokemon.event.dto.ScheduleDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.entity.EventSchedule;

@Mapper
public interface EventScheduleRepository {

  List<EventItemResponseDTO> selectTodayOpenedTickets();

  List<EventItemResponseDTO> selectTicketsToBeOpened();

  Long save(EventSchedule eventSchedule);

  List<EventItemResponseDTO> selectEventList(@Param("genre") String genre,
      @Param("limit") int limit, @Param("offset") int offset);

  int getTotalCountByGenre(@Param("genre") String genre);

  List<EventItemResponseDTO> selectEventSearchList(@Param("keyword") String keyword,
      @Param("limit") int limit, @Param("offset") int offset, @Param("genre") String genre);

  int getSearchTotalCount(@Param("keyword") String keyword, @Param("genre") String genre);

  int updateEventSchedule(EventSchedule eventSchedule);

  Long findVenueIdByEventScheduleId(Long eventScheduleId);

  List<Long> findEventScheduleIdTwoHoursLater();

  Optional<EventScheduleInfoDto> findEventScheduleById(
          @Param("eventScheduleId") Long eventScheduleId);

  List<ScheduleDetailDto> findScheduleDetailsByIds(@Param("ids") List<Long> ids);

  List<EventScheduleInfoDto> findEventSchedulesByIdIn(@Param("ids") List<Long> ids);
}
