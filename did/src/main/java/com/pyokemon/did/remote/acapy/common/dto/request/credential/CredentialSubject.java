package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.Wallet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 자격 증명의 주체(Subject) 정보
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialSubject {
    private String id;

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("event_schedule_id")
    private Long eventScheduleId;

    @JsonProperty("seat_id")
    private Long seatId;

    /**
     * 사용자 지갑과 예매 정보를 기반으로 CredentialSubject 객체 생성
     */
    public static CredentialSubject of(Wallet userWallet, Long bookingId, Long eventScheduleId, Long seatId) {
        return CredentialSubject.builder()
                .id(userWallet.getPublicDid())
                .bookingId(bookingId)
                .eventScheduleId(eventScheduleId)
                .seatId(seatId)
                .build();
    }
}
