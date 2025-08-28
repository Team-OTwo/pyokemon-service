package com.pyokemon.did.api.open;

import com.pyokemon.did.domain.dto.request.VerificationRequest;
import com.pyokemon.did.domain.dto.request.VerificationRequest.CreateVerificationRequest;
import com.pyokemon.did.domain.dto.response.VerificationResponse;
import com.pyokemon.did.domain.dto.response.VerificationResponse.CreateVerificationResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequestMapping("/api/verifications")
@RestController
@AllArgsConstructor
public class VerificationController {

    @PostMapping
    public CreateVerificationResponse CreateVerificationUrl(CreateVerificationRequest request) {



    }
}
