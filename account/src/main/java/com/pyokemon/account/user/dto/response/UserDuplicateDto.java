package com.pyokemon.account.user.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDuplicateDto {
  private String loginId;
  private boolean isDuplicated;
}
