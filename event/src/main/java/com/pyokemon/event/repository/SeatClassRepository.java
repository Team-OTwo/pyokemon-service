package com.pyokemon.event.repository;

import com.pyokemon.event.entity.SeatClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SeatClassRepository {
    @Select("SELECT seat_class_id, class_name, created_at, updated_at " +
            "FROM tb_seat_class")
    List<SeatClass> findAll();

    @Select("SELECT seat_class_id, class_name, created_at, updated_at " +
            "FROM tb_seat_class " +
            "WHERE seat_class_id = #{id}")
    Optional<SeatClass> findById(@Param("id") Long id);
}