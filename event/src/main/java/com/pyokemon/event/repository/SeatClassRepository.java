package com.pyokemon.event.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.SeatClass;

@Mapper
public interface SeatClassRepository {
  Optional<SeatClass> findByClassName(String className);
  
  List<SeatClass> findAll();
  
  Optional<SeatClass> findById(Long seatClassId);
}
