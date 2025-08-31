package com.pyokemon.did.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@RedisHash("verification")
public class Verification {

  @Id
  private Long id;

  @Indexed
  @JsonProperty("pres_ex_id")
  private String presExId;

  private VpStatus status;

  public static enum VpStatus {
    SUCCESS, FAIL, INVALID_VC
  }


  public static Verification of(String presExId, VpStatus status) {
    return Verification.builder().presExId(presExId).status(status).build();
  }
}
