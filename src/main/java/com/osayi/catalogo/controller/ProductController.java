// src/main/java/com/osayi/catalogo/controller/ProductController.java
package com.osayi.catalogo.controller;

import com.osayi.catalogo.model.AppUser;
import com.osayi.catalogo.model.Product;
import com.osayi.catalogo.model.Review;
import com.osayi.catalogo.model.Credentials;                  // <<< NEW
import com.osayi.catalogo.repository.ProductRepository;
import com.osayi.catalogo.repository.ReviewRepository;
import com.osayi.catalogo.repository.CredentialsRepository;   // <<< NEW
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;        // <<< NEW
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ProductController {

  private final ProductRepository repo;
  private final ReviewRepository reviews;
  private final CredentialsRepository creds;                  // <<< NEW

  public ProductController(ProductRepository repo,
                           ReviewRepository reviews,
                           CredentialsRepository creds) {     // <<< NEW
    this.repo = repo;
    this.reviews = reviews;
    this.creds = creds;                                       // <<< NEW
  }

  @GetMapping("/products/{id}")
  public String product(@PathVariable Long id,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        Authentication authentication,         // <<< NEW
                        Model model) {

    Product p = repo.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prodotto inesistente"));

    List<Product> simili = repo.findTop4ByCategoryAndIdNotOrderByIdDesc(p.getCategory(), p.getId());

    Page<Review> revPage = reviews.findByProductIdOrderByCreatedAtDesc(id, PageRequest.of(page, size));

    // passa l'ID dell'utente loggato (se presente)
    Long meId = null;
    if (authentication != null && authentication.isAuthenticated()) {
      String principal = authentication.getName();
      Credentials c = creds.findByUsername(principal)
          .orElseGet(() -> creds.findByEmail(principal).orElse(null));
      if (c != null && c.getAppUser() != null) meId = c.getAppUser().getId();
    }

    model.addAttribute("product", p);
    model.addAttribute("simili", simili);
    model.addAttribute("reviews", revPage.getContent());
    model.addAttribute("reviewsPage", revPage);
    model.addAttribute("meId", meId);                        // <<< QUI
    return "product-detail";
  }

  @GetMapping("/products")
  public String catalog(Model model) {
    model.addAttribute("prodotti", repo.findAllByOrderByIdDesc());
    return "catalog-cards";
  }
}
