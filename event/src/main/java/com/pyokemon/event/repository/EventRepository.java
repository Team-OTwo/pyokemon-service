package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.EventDetailResponseDTO;
import com.pyokemon.event.dto.TenantEventDetailResponseDTO;
import com.pyokemon.event.dto.TenantEventListDto;
import com.pyokemon.event.entity.Event;

@Mapper
public interface EventRepository {
  List<Event> findByTenantId(Long tenantId);

  List<Event> findByAccountId(Long accountId);

  List<TenantEventListDto> findTenantEventListByAccountId(Long accountId);

  List<Event> findByStatus(Event.EventStatus status);

  List<Event> findByGenre(String genre);

  List<Event> findByTitleContainingIgnoreCase(String title);

  List<Event> findByAgeLimit(Long ageLimit);

  int save(Event event);

  EventDetailResponseDTO findEventDetailByEventId(Long eventId);

  TenantEventDetailResponseDTO findTenantEventDetailByEventId(Long eventId);

  Event findById(Long eventId);

  int updateEvent(Event event);
}
