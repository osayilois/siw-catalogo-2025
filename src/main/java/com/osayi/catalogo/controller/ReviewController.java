package com.osayi.catalogo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.osayi.catalogo.model.Review;
import com.osayi.catalogo.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

  private final ReviewService svc;

  public ReviewController(ReviewService svc) {
    this.svc = svc;
  }

  // Lista recensioni di un prodotto (pubblica)
  @GetMapping("/product/{productId}")
  public List<Review> list(@PathVariable Long productId) {
    return svc.listByProduct(productId);
  }

  // Aggiungi recensione (richiede login)
  @PostMapping("/product/{productId}")
  public Review add(@PathVariable Long productId,
                    @RequestBody Map<String, String> body,
                    Authentication auth) {
    String text = body.getOrDefault("text", "");
    return svc.add(productId, text, auth.getName());
  }

  // Modifica recensione (solo autore)
  @PutMapping("/{reviewId}")
  public Review update(@PathVariable Long reviewId,
                       @RequestBody Map<String, String> body,
                       Authentication auth) {
    String text = body.getOrDefault("text", "");
    return svc.update(reviewId, text, auth.getName());
  }

  // Elimina recensione (solo autore)
  @DeleteMapping("/{reviewId}")
  public void delete(@PathVariable Long reviewId, Authentication auth) {
    svc.delete(reviewId, auth.getName());
  }
}
