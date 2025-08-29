package com.pyokemon.did.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@RedisHash("issued-proof")
public class IssuedProof {
  @Id
  private String pres_ex_id;

  private String challenge;

  @TimeToLive
  private Long expirationInSeconds;

  public static IssuedProof of(String presExId, String challenge, Long ttl) {
    return IssuedProof.builder().pres_ex_id(presExId).challenge(challenge).expirationInSeconds(ttl)
        .build();
  }

}
