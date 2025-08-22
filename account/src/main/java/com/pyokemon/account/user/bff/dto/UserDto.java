package com.pyokemon.account.user.bff.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  private Long accountId;
  private String name;
}
