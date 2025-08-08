package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletMetadata extends BaseEntity {
    private String key;
    private String token;
    private Long tenantId;
}
