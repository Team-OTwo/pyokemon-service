package com.pyokemon.event.repository;

import com.pyokemon.event.entity.Event;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface EventRepository{
    List<Event> findByTenantId(Long tenantId);
    List<Event> findByStatus(Event.EventStatus status);
    List<Event> findByGenre(String genre);
    List<Event> findByTitleContainingIgnoreCase(String title);
    List<Event> findByAgeLimit(Long ageLimit);
}
