package com.pyokemon.event.repository;

import org.apache.ibatis.annotations.Mapper;
import com.pyokemon.event.entity.EventSchedule;

@Mapper
public interface EventScheduleRepository {
  Long save(EventSchedule eventSchedule);
}