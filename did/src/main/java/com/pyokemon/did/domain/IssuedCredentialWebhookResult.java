package com.pyokemon.did.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@RedisHash("issued-credential-webhook-result")
public class IssuedCredentialWebhookResult {

    @Id
    private Long id;

    @JsonProperty("cred_ex_id")
    @Indexed
    private String credExId;

    @JsonProperty("booking_id")
    private Long bookingId;

    @TimeToLive
    private Long expirationInSeconds;

    public static IssuedCredentialWebhookResult of(String credExId, Long bookingId) {
        return IssuedCredentialWebhookResult.builder().credExId(credExId).bookingId(bookingId).build();
    }

}
