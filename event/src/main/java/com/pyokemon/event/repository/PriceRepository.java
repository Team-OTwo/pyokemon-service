package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.Price;

@Mapper
public interface PriceRepository {
  Long save(Price price);

  List<Price> findByEventScheduleId(Long eventScheduleId);

  Optional<Price> findById(Long priceId);
}
