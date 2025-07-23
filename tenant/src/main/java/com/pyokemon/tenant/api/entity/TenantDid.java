package com.pyokemon.tenant.api.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDid {

    private Long tenantDidId;
    private Long tenantId;
    private String did;
    private Boolean isValid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
