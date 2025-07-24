package com.pyokemon.event.repository;

import com.pyokemon.event.entity.Price;

import java.util.List;

public interface PriceRepository {
    List<Price> findByEventScheduleId(Long eventScheduleId);
}
