package com.pyokemon.did.remote.acapy.common.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pyokemon.did.domain.Wallet.AccountRole;
import com.pyokemon.did.remote.acapy.common.constants.AcaPyConstants;
import com.pyokemon.did.remote.acapy.common.dto.base.BaseWalletRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * ACA-Py에 지갑 생성을 요청하기 위한 DTO
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateWalletRequest extends BaseWalletRequest {
    
    @JsonProperty("wallet_type")
    private String walletType;
    
    @JsonProperty("wallet_dispatch_type")
    private String walletDispatchType;
    
    @JsonProperty("key_management_mode")
    private String keyManagementMode;
    
    /**
     * 특정 계정 역할을 위한 지갑 생성 요청 생성
     */
    public static CreateWalletRequest forAccountRole(AccountRole accountRole) {
        return CreateWalletRequest.builder()
                .label(accountRole + " wallet")
                .walletKey(UUID.randomUUID().toString())
                .walletName("wallet:" + UUID.randomUUID())
                .walletType(AcaPyConstants.Wallet.TYPE_ASKAR)
                .walletDispatchType(AcaPyConstants.Wallet.DISPATCH_TYPE_DEFAULT)
                .keyManagementMode(AcaPyConstants.Wallet.KEY_MANAGEMENT_MODE_MANAGED)
                .build();
    }
}
