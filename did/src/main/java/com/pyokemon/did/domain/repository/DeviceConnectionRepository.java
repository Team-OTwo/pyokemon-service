package com.pyokemon.did.domain.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.did.domain.DeviceConnection;

@Mapper
public interface DeviceConnectionRepository {

  /**
   * DeviceConnection을 저장합니다.
   *
   * @param deviceConnection 저장할 DeviceConnection
   * @return 저장된 DeviceConnection의 ID
   */
  Long save(DeviceConnection deviceConnection);

  /**
   * ID로 DeviceConnection을 조회합니다.
   *
   * @param id 조회할 DeviceConnection의 ID
   * @return DeviceConnection (Optional)
   */
  Optional<DeviceConnection> findById(Long id);

  /**
   * connection_id로 DeviceConnection을 조회합니다.
   *
   * @param connectionId 조회할 connection_id
   * @return DeviceConnection (Optional)
   */
  Optional<DeviceConnection> findByConnectionId(String connectionId);

  /**
   * device_id로 DeviceConnection 목록을 조회합니다.
   *
   * @param deviceId 조회할 device_id
   * @return DeviceConnection 목록
   */
  List<DeviceConnection> findByDeviceId(String deviceId);

  /**
   * user_id로 DeviceConnection을 조회합니다. 한 사용자당 하나의 DeviceConnection만 존재합니다.
   *
   * @param userId 조회할 user_id
   * @return DeviceConnection (Optional)
   */
  Optional<DeviceConnection> findByUserId(Long userId);

  /**
   * alias로 DeviceConnection을 조회합니다.
   *
   * @param alias 조회할 alias (Tracking ID)
   * @return DeviceConnection (Optional)
   */
  Optional<DeviceConnection> findByAlias(String alias);

  /**
   * public_did로 DeviceConnection을 조회합니다.
   *
   * @param publicDid 조회할 public_did
   * @return DeviceConnection (Optional)
   */
  Optional<DeviceConnection> findByPublicDid(String publicDid);

  /**
   * status로 DeviceConnection 목록을 조회합니다.
   *
   * @param status 조회할 status
   * @return DeviceConnection 목록
   */
  List<DeviceConnection> findByStatus(DeviceConnection.DeviceConnectionStatus status);

  /**
   * 모든 DeviceConnection을 조회합니다.
   *
   * @return DeviceConnection 목록
   */
  List<DeviceConnection> findAll();

  /**
   * DeviceConnection을 업데이트합니다.
   *
   * @param deviceConnection 업데이트할 DeviceConnection
   * @return 업데이트된 행 수
   */
  int update(DeviceConnection deviceConnection);

  /**
   * ID로 DeviceConnection을 삭제합니다.
   *
   * @param id 삭제할 DeviceConnection의 ID
   * @return 삭제된 행 수
   */
  int deleteById(Long id);

  /**
   * connection_id로 DeviceConnection을 삭제합니다.
   *
   * @param connectionId 삭제할 connection_id
   * @return 삭제된 행 수
   */
  int deleteByConnectionId(String connectionId);

  /**
   * device_id로 DeviceConnection을 삭제합니다.
   *
   * @param deviceId 삭제할 device_id
   * @return 삭제된 행 수
   */
  int deleteByDeviceId(String deviceId);


}