package com.pyokemon.did.remote.acapy.common.dto.request.credential;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 표준 자격 증명(Evidence 없는 기본 Credential)
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StandardCredential extends BaseCredential {
    // 기본 구현만 사용
}
