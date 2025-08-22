package com.pyokemon.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class IdsRequest {
    private List<Long> ids;
}