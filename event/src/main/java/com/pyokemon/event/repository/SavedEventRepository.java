package com.pyokemon.event.repository;

import com.pyokemon.event.entity.SavedEvent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SavedEventRepository {
    Long save(SavedEvent savedEvent);
}
