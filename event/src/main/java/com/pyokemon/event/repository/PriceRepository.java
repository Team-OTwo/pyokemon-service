package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.dto.PriceWithSeatClassDTO;
import com.pyokemon.event.entity.Price;

@Mapper
public interface PriceRepository {

  Long save(Price price);

  int updatePrice(Price price);

  List<PriceWithSeatClassDTO> findPricesWithSeatClassByEventScheduleId(Long eventScheduleId);
}
