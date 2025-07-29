package com.pyokemon.admin.entity;

import java.time.LocalDateTime;

import com.pyokemon.common.entity.BaseEntity;

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
public class Admin extends BaseEntity {
  private Long id;
  private String adminId;
  private String password;
  private String username;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
