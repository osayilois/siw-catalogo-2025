// src/main/java/com/osayi/catalogo/repository/AppUserRepository.java
package com.osayi.catalogo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.osayi.catalogo.model.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByEmail(String email);  
}
