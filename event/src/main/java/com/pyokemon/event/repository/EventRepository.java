package com.pyokemon.event.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.pyokemon.event.entity.Event;

@Repository
public interface EventRepository {
  List<Event> findByTenantId(Long tenantId);

  List<Event> findByStatus(Event.EventStatus status);

  List<Event> findByGenre(String genre);

  List<Event> findByTitleContainingIgnoreCase(String title);

  List<Event> findByAgeLimit(Long ageLimit);
}
