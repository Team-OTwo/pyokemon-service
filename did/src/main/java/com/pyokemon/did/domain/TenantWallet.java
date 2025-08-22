package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;
import lombok.*;

import org.springframework.data.annotation.Id;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantWallet extends BaseEntity {

    private Long id;
    private Long tenantId;
    private String token;
    private String publicDid;
    private String publicVerkey;
}
