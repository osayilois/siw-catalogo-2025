// src/main/java/com/osayi/catalogo/controller/AdminProductController.java
package com.osayi.catalogo.controller;

import com.osayi.catalogo.model.Product;
import com.osayi.catalogo.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

  private final ProductRepository repo;

  // Valori del dropdown (uguale concept a Eventure)
  private static final List<String> CATEGORIES = List.of(
      "Casa",
      "Tech",
      "Sport",
      "Cucina",
      "Libri",
      "Scuola",
      "Abbigliamento",
      "Altro"
  );

  public AdminProductController(ProductRepository repo) { this.repo = repo; }

  // LISTA prodotti (tabella)
  @GetMapping

  public String list(Model model) {
    model.addAttribute("prodotti", repo.findAllByOrderByIdDesc());
    return "admin-products";
  }

  // FORM nuovo
  @GetMapping("/nuovo")

  public String newForm(Model model) {
    model.addAttribute("product", new Product());
    model.addAttribute("categories", CATEGORIES);
    return "product-form";
  }

  // SALVA (crea/aggiorna)
  @PostMapping

  public String save(@Valid @ModelAttribute("product") Product p,
                     BindingResult br,
                     Model model) {
    if (br.hasErrors()) {
      model.addAttribute("categories", CATEGORIES);
      return "product-form";
    }
    repo.save(p);
    return "redirect:/admin/products";
  }

  // MODIFICA: carica nel form
  @GetMapping("/modifica/{id}")

  public String edit(@PathVariable Long id, Model model) {
    Product p = repo.findById(id).orElseThrow();
    model.addAttribute("product", p);
    model.addAttribute("categories", CATEGORIES);
    return "product-form";
  }

  // ELIMINA
  @PostMapping("/elimina/{id}")

  public String delete(@PathVariable Long id) {
    repo.deleteById(id);
    return "redirect:/admin/products";
  }

  // VISUALIZZA RECENSIONI
  @GetMapping("/reviews/{id}")

public String adminReviews(@PathVariable Long id, Model model){
  Product p = repo.findById(id).orElseThrow();
  model.addAttribute("product", p);
  // model.addAttribute("reviews", reviewRepo.findByProductIdOrderByCreatedAtDesc(id));
  return "admin-product-reviews"; // creerai il template quando servirà
}

}
