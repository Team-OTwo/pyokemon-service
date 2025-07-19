package com.pyokemon.common.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseEntity {

  private Long id;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime deletedAt;

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void delete() {
    this.deletedAt = LocalDateTime.now();
  }
}
