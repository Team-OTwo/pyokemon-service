package com.pyokemon.did.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@RedisHash("issued-proof")
public class IssuedProof {

  @Id
  private Long id;

  @Indexed
  @JsonProperty("pres_ex_id")
  private String presExId;

  private String challenge;

  @TimeToLive
  private Long expirationInSeconds;

  public static IssuedProof of(String presExId, String challenge, Long ttl) {
    return IssuedProof.builder().presExId(presExId).challenge(challenge).expirationInSeconds(ttl)
        .build();
  }

}
