package com.pyokemon.tenant.api.entity;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

  private Long id;
  private String loginId;
  private String password;
  private String corpName;
  private String corpId;
  private String city;
  private String street;
  private String zipcode;
  private String ceoName;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

}
