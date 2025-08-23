package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;
import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWallet extends BaseEntity {

    private Long userId;
    private String token;

}
