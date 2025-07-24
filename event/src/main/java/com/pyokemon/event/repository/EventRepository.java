package com.pyokemon.event.repository;

import com.pyokemon.event.entity.Event;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventRepository {
  Long save(Event event);
}