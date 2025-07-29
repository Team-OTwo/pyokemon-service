package com.pyokemon.account.user.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.account.user.entity.UserDevice;

@Mapper
public interface UserDeviceRepository {

  Optional<UserDevice> findByUserDeviceId(Long userDeviceId);

  List<UserDevice> findByUserId(Long userId);

  Optional<UserDevice> findByUserIdAndDeviceNumber(Long userId, String deviceNumber);

  int insert(UserDevice userDevice);

  int update(UserDevice userDevice);

  int delete(Long userDeviceId);

  int deleteByUserIdAndDeviceNumber(Long userId, String deviceNumber);
}
