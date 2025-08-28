package com.pyokemon.did.remote.acapy.common.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class JwtVerifyRequest {

    private String jwt;

}
