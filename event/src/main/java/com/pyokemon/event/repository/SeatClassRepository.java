package com.pyokemon.event.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.event.entity.SeatClass;

@Mapper
public interface SeatClassRepository {
  List<SeatClass> findAll();
}
