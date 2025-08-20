package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.entity.SavedEvent;

@Mapper
public interface SavedEventRepository {
  Long save(SavedEvent savedEvent);

  Long delete(Long accountId, Long eventId);

  boolean existsByAccountIdAndEventId(Long accountId, Long eventId);

  List<EventItemResponseDTO> findByAccountId(Long accountId, int offset, int limit);

  int countTotalEventsByAccountId(Long accountId);
}
