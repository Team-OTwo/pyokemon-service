package com.pyokemon.event.repository;

import com.pyokemon.event.dto.EventItemResponseDTO;
import com.pyokemon.event.entity.SavedEvent;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SavedEventRepository {
    Long save(SavedEvent savedEvent);
    Long delete(Long accountId, Long eventId);
    boolean existsByAccountIdAndEventId(Long accountId, Long eventId);
    List<EventItemResponseDTO> findByAccountId(Long accountId, int offset, int limit);
    int countTotalEventsByAccountId(Long accountId);
}
