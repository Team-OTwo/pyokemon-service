package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.EventSchedule;

@Mapper
public interface EventScheduleRepository {
  Long save(EventSchedule eventSchedule);

  List<EventSchedule> findByEventId(Long eventId);

  Optional<EventSchedule> findById(Long eventScheduleId);
}