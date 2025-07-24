package com.pyokemon.event.repository;

import com.pyokemon.event.entity.EventSchedule;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface EventScheduleRepository {
    Optional<EventSchedule> findById(Long id);
}
