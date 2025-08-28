package com.pyokemon.did.domain.repository;

import org.springframework.data.repository.CrudRepository;

import com.pyokemon.did.domain.IssuedProof;
import com.pyokemon.did.domain.Verification;

public interface VerificationRepository extends CrudRepository<Verification, String> {
}
