package com.pyokemon.event.repository;

import com.pyokemon.event.entity.Event;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository{
    List<Event> findByTenantId(Long tenantId);
    List<Event> findByStatus(Event.EventStatus status);
    List<Event> findByGenre(String genre);
    List<Event> findByTitleContainingIgnoreCase(String title);
    List<Event> findByAgeLimit(Long ageLimit);
}
