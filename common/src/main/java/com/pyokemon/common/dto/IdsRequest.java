package com.pyokemon.common.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class IdsRequest {
  private List<Long> ids;
}
