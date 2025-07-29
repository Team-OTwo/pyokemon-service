package com.pyokemon.account.tenant.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

  private Long tenantId;
  private Long accountId;
  private String name;
  private String corpId;
  private String city;
  private String street;
  private String zipcode;
  private String ceo;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
