// src/main/java/com/osayi/catalogo/config/DataLoader.java
package com.osayi.catalogo.config;

import com.osayi.catalogo.model.Credentials;
import com.osayi.catalogo.repository.CredentialsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

  @Bean
  CommandLineRunner initAdmin(CredentialsRepository repo, PasswordEncoder enc) {
    return args -> {
      String desiredUsername = "admin@catalogo.com";   // <-- metti il tuo
      String desiredPassword = "Admin123!";          // <-- metti il tuo

      // se esiste con il nuovo username lo aggiorno, altrimenti lo creo
      Credentials admin = repo.findByUsername(desiredUsername).orElse(null);
      if (admin == null) {
        admin = new Credentials();
        admin.setUsername(desiredUsername);
      }
      admin.setPassword(enc.encode(desiredPassword));        // HASH BCRYPT
      admin.setRole(Credentials.Role.ADMIN);

      repo.save(admin);
    };
  }
}
