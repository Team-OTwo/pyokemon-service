package com.pyokemon.did.domain.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.pyokemon.did.domain.Verification;

public interface VerificationRepository extends CrudRepository<Verification, String> {


  /**
   * pres_ex_id로 Verification 객체를 찾습니다.
   * 
   * @Indexed가 붙어있기 때문에 검색이 가능합니다. * @param presExId 증명 제시 ID
   * @return Verification 객체 (Optional)
   */
  Optional<Verification> findByPresExId(String presExId);
}
