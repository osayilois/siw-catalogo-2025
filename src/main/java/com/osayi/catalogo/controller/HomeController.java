// src/main/java/com/osayi/catalogo/controller/HomeController.java
package com.osayi.catalogo.controller;

import com.osayi.catalogo.model.Product;
import com.osayi.catalogo.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class HomeController {

  private final ProductRepository products;

  public HomeController(ProductRepository products) {
    this.products = products;
  }

  @GetMapping({"/", "/home"})
  public String home(@RequestParam(name = "q", required = false) String q, Model model) {

    // Valore della search per l’input nell’header (solo in home)
    model.addAttribute("q", q);

    // Appena aggiunti (ultimi 8 per id desc)
    List<Product> recenti = products.findTop8ByOrderByIdDesc();
    model.addAttribute("prodottiRecenti", recenti);

    // Risultati ricerca SOLO per nome
    if (q != null && !q.isBlank()) {
      model.addAttribute("results",
          products.findByNameContainingIgnoreCaseOrderByIdDesc(q.trim()));
    }

    return "home";
  }

  @GetMapping("/login")
  public String login() { return "login"; }

  @GetMapping("/register")
  public String registerForm() { return "register"; }
}
