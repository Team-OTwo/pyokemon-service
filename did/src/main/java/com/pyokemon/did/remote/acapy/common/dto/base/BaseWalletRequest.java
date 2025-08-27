package com.pyokemon.did.remote.acapy.common.dto.base;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 지갑(Wallet) 관련 요청의 기본 추상 클래스
 * 모든 지갑 요청은 이 클래스를 상속받아야 함
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseWalletRequest implements AcaPyRequest {
    
    private String label;
    
    @JsonProperty("wallet_key")
    private String walletKey;
    
    @JsonProperty("wallet_name")
    private String walletName;
}
