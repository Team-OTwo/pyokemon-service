package com.pyokemon.event.repository;


import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.Price;

@Mapper
public interface PriceRepository {
  List<Price> findByEventScheduleId(Long eventScheduleId);

  Long save(Price price);

}
