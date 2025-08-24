package com.pyokemon.did.domain.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.pyokemon.did.domain.UserWallet;

@Mapper
public interface UserWalletRepository {

  /**
   * UserWallet을 저장합니다.
   *
   * @param userWallet 저장할 UserWallet
   * @return 저장된 UserWallet의 ID
   */
  Long save(UserWallet userWallet);

  /**
   * UserWallet을 저장합니다.
   *
   * @param userWallet 저장할 UserWallet
   * @return 영향받은 행 수
   */
  int saveAndReturn(UserWallet userWallet);

  /**
   * ID로 UserWallet을 조회합니다.
   *
   * @param id 조회할 UserWallet의 ID
   * @return UserWallet (Optional)
   */
  Optional<UserWallet> findById(Long id);

  /**
   * user_id로 UserWallet을 조회합니다.
   *
   * @param userId 조회할 user_id
   * @return UserWallet (Optional)
   */
  Optional<UserWallet> findByUserId(Long userId);

  /**
   * token으로 UserWallet을 조회합니다.
   *
   * @param token 조회할 token
   * @return UserWallet (Optional)
   */
  Optional<UserWallet> findByToken(String token);

  /**
   * 모든 UserWallet 목록을 조회합니다.
   *
   * @return UserWallet 목록
   */
  List<UserWallet> findAll();

  /**
   * UserWallet을 업데이트합니다.
   *
   * @param userWallet 업데이트할 UserWallet
   * @return 업데이트된 행 수
   */
  int update(UserWallet userWallet);

  /**
   * ID로 UserWallet을 삭제합니다.
   *
   * @param id 삭제할 UserWallet의 ID
   * @return 삭제된 행 수
   */
  int deleteById(Long id);

  /**
   * user_id로 UserWallet을 삭제합니다.
   *
   * @param userId 삭제할 user_id
   * @return 삭제된 행 수
   */
  int deleteByUserId(Long userId);

  /**
   * token으로 UserWallet을 삭제합니다.
   *
   * @param token 삭제할 token
   * @return 삭제된 행 수
   */
  int deleteByToken(String token);
}
