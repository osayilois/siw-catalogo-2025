package com.osayi.catalogo.repository;
import com.osayi.catalogo.model.AppUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
   
}
