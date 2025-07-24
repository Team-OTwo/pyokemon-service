package com.pyokemon.event.repository;

import org.apache.ibatis.annotations.Mapper;
import com.pyokemon.event.entity.Price;

@Mapper
public interface PriceRepository {
  Long save(Price price);
}
