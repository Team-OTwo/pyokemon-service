package com.pyokemon.admin.entity;

import com.pyokemon.common.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Admin extends BaseEntity {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role;
} 