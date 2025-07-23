package com.pyokemon.tenant.api.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    private Long id;
    private String email;
    private String password;
    private String corpName;
    private String corpId;
    private String city;
    private String street;
    private String zipcode;
    private String ceo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
