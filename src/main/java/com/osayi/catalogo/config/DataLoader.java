// src/main/java/com/osayi/catalogo/config/DataLoader.java
package com.osayi.catalogo.config;

import com.osayi.catalogo.model.AppUser;
import com.osayi.catalogo.model.Credentials;
import com.osayi.catalogo.repository.AppUserRepository;
import com.osayi.catalogo.repository.CredentialsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

  @Bean
  CommandLineRunner initAdmin(CredentialsRepository credRepo,
                              AppUserRepository userRepo,
                              PasswordEncoder encoder) {
    return args -> {
      final String desiredUsername = "admin@catalogo.local";
      final String desiredPassword = "admin123";

      // Prendi o crea le credenziali admin
      Credentials admin = credRepo.findByUsername(desiredUsername).orElseGet(() -> {
        Credentials c = new Credentials();
        c.setUsername(desiredUsername);
        c.setRole(Credentials.Role.ADMIN);
        return c;
      });
      admin.setPassword(encoder.encode(desiredPassword));

      // Se non è collegato un AppUser, prova prima a riusarne uno esistente con la stessa email
      if (admin.getAppUser() == null) {
        AppUser au = userRepo.findByEmail(desiredUsername).orElseGet(() -> {
          AppUser x = new AppUser();
          x.setEmail(desiredUsername);
          x.setName("Admin");
          x.setSurname(" ");
          return userRepo.save(x);
        });
        admin.setAppUser(au);
      }

      credRepo.save(admin);
    };
  }
}
