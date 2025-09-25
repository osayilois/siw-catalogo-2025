package com.osayi.catalogo.repository;

import com.osayi.catalogo.model.Credentials;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {

  // Esiste nella tua entity 'Credentials'
  Optional<Credentials> findByUsername(String username);

  // Manteniamo la firma findByEmail, ma in realtà cerchiamo su 'username'
  @Query("select c from Credentials c where c.username = :email")
  Optional<Credentials> findByEmail(@Param("email") String email);

  // Utile se da qualche parte usi existsByUsername
  boolean existsByUsername(String username);

  // Manteniamo la firma existsByEmail mappata su 'username'
  @Query("select (count(c) > 0) from Credentials c where c.username = :email")
  boolean existsByEmail(@Param("email") String email);
}
