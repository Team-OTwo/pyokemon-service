package com.pyokemon.event.repository;

import com.pyokemon.event.entity.SeatClass;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SeatClassRepository {
    List<SeatClass> findAll();
}