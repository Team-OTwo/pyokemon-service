package com.pyokemon.did.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@RedisHash("verification")
public class Verification {

    @Id
    private String pres_ex_id;

    private VpStatus status;

    public static enum VpStatus {
        SUCCESS,
        FAIL,
        INVALID_VC
    }


    public static Verification of(String presExId, VpStatus status) {
        return Verification.builder()
                .pres_ex_id(presExId)
                .status(status)
                .build();
    }
}
