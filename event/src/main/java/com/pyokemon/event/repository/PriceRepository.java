package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.Price;

@Mapper
public interface PriceRepository {
    
    Long save(Price price);
    
    int updatePrice(Price price);
    
    int deletePrice(Long priceId);
    
    List<Price> findByEventScheduleId(Long eventScheduleId);
}
