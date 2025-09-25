// src/main/java/com/osayi/catalogo/controller/ProductController.java
package com.osayi.catalogo.controller;

import com.osayi.catalogo.model.Product;
import com.osayi.catalogo.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ProductController {

  private final ProductRepository repo;

  public ProductController(ProductRepository repo) { this.repo = repo; }

  // DETTAGLIO prodotto + simili
  @GetMapping("/products/{id}")
  public String detail(@PathVariable Long id, Model model) {
    Product p = repo.findById(id).orElseThrow();
    List<Product> simili = repo.findTop8ByCategoryAndIdNotOrderByIdDesc(p.getCategory(), p.getId());

    model.addAttribute("product", p);
    model.addAttribute("simili", simili);
    return "product-detail"; // crea template
  }

  // LISTA PUBBLICA: /products
  @GetMapping("/products")
  public String catalog(Model model) {
    model.addAttribute("prodotti", repo.findAllByOrderByIdDesc());
    return "catalog-cards"; // templates/catalog-cards.html
  }
}
