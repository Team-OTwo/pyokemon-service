package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;
import lombok.*;

import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWallet extends BaseEntity {

    private Long id;
    private Long userId;
    private String token;
}
