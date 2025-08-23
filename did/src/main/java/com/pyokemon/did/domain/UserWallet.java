package com.pyokemon.did.domain;

import com.pyokemon.common.entity.BaseEntity;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWallet extends BaseEntity {

  private Long id;
  private Long userId;
  private String token;

  @Builder
  public UserWallet(Long userId, String token) {
    this.userId = userId;
    this.token = token;
  }
}
