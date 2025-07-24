package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;


import com.pyokemon.event.entity.Event;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventRepository {
  List<Event> findByTenantId(Long tenantId);

  List<Event> findByStatus(Event.EventStatus status);

  List<Event> findByGenre(String genre);

  List<Event> findByTitleContainingIgnoreCase(String title);

  List<Event> findByAgeLimit(Long ageLimit);

  Long save(Event event);
  
  Optional<Event> findById(Long eventId);
}