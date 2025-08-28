package com.pyokemon.did.domain.repository;

import com.pyokemon.did.domain.IssuedProof;
import com.pyokemon.did.domain.Verification;
import org.springframework.data.repository.CrudRepository;

public interface VerificationRepository extends CrudRepository<Verification, String> {
}
