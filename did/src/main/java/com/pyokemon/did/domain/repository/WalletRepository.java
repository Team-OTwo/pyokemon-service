package com.pyokemon.did.domain.repository;

import com.pyokemon.did.domain.Wallet;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;


/**
 * 계정 지갑 데이터에 접근하기 위한 Repository 인터페이스
 */
@Mapper
public interface WalletRepository {

  /**
   * Wallet 객체를 저장합니다.
   *
   * @param wallet 저장할 Wallet 객체
   * @return 저장된 Wallet ID
   */
  Long save(Wallet wallet);


  /**
   * 계정 ID로 Wallet 조회합니다.
   *
   * @param accountId 조회할 계정 ID
   * @return 조회된 Wallet (Optional)
   */
  Optional<Wallet> findByAccountId(Long accountId);

  /**
   * 계정 ID로 지갑 존재 여부를 확인합니다.
   *
   * @param accountId 확인할 테넌트 ID
   * @return 지갑 존재 여부 (존재하면 true, 없으면 false)
   */
  boolean existsByAccountId(Long accountId);


  /**
   * Wallet 객체를 업데이트합니다.
   *
   * @param wallet 업데이트할 Wallet 객체
   * @return 업데이트된 행 수
   */
  int update(Wallet wallet);

  /**
   * 계정 ID로 Wallet 을 삭제합니다.
   *
   * @param accountId 삭제할 계정 ID
   * @return 삭제된 행 수
   */
  int deleteByAccountId(Long accountId);

}
