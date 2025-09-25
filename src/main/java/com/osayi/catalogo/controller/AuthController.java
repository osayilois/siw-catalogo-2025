// src/main/java/com/osayi/catalogo/controller/AuthController.java
package com.osayi.catalogo.controller;

import com.osayi.catalogo.model.Credentials;
import com.osayi.catalogo.repository.CredentialsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
  private final CredentialsRepository repo;
  private final PasswordEncoder encoder;
  public AuthController(CredentialsRepository repo, PasswordEncoder encoder) {
    this.repo = repo; this.encoder = encoder;
  }

  @PostMapping("/register")
public String register(@RequestParam String username,
                       @RequestParam String password,
                       @RequestParam String confirmPassword,
                       Model model) {
  model.addAttribute("username", username);

  if (!password.equals(confirmPassword)) {
    model.addAttribute("error", "Le password non coincidono.");
    return "register";
  }
  if (repo.existsByUsername(username)) {
    model.addAttribute("error", "Utente già esistente.");
    return "register";
  }

  var c = new Credentials();
  c.setUsername(username.trim());
  c.setPassword(encoder.encode(password)); // sempre hash
  c.setRole(Credentials.Role.USER);
  repo.save(c);

  return "redirect:/login?registered";
}


}
