package com.osayi.catalogo.repository;

import com.osayi.catalogo.model.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
  Optional<Credentials> findByUsername(String username);
  boolean existsByUsername(String username);
}


