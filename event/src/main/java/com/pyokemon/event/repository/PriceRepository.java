package com.pyokemon.event.repository;


import com.pyokemon.event.entity.Price;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PriceRepository {
  List<Price> findByEventScheduleId(Long eventScheduleId);
  Long save(Price price);

}
