package com.pyokemon.tenant.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequestDto {

    @NotBlank(message = "대표자명은 필수입니다")
    private String ceoName;

    @NotBlank(message = "회사명은 필수입니다")
    private String corpName;

    @NotBlank(message = "주소는 필수입니다")
    private String city;

    @NotBlank(message = "주소는 필수입니다")
    private String street;

    @NotBlank(message = "주소는 필수입니다")
    private String zipcode;

}
