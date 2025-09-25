// ---------------------------------------------------------------------------------
// ------ CONTROLLER PER LE PAGINE HTML RELATIVE ALLE RECENSIONI (ROTTE ETC) -------

package com.osayi.catalogo.controller;

import com.osayi.catalogo.model.AppUser;
import com.osayi.catalogo.model.Credentials;
import com.osayi.catalogo.model.Product;
import com.osayi.catalogo.model.Review;
import com.osayi.catalogo.repository.CredentialsRepository;
import com.osayi.catalogo.repository.ProductRepository;
import com.osayi.catalogo.repository.ReviewRepository;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Method;

@Controller
@Validated
public class ReviewPageController {

  private final ReviewRepository reviews;
  private final ProductRepository products;
  private final CredentialsRepository credentials;

  public ReviewPageController(ReviewRepository reviews,
                              ProductRepository products,
                              CredentialsRepository credentials) {
    this.reviews = reviews;
    this.products = products;
    this.credentials = credentials;
  }

  // ===== Form backing bean (sempre disponibile) =====
  public record ReviewForm(
      @Min(value = 1, message = "Seleziona da 1 a 5 stelle")
      @Max(value = 5, message = "Seleziona da 1 a 5 stelle")
      Integer stars,

      @NotBlank(message = "Inserisci un commento (non lasciare solo spazi).")
      @Size(max = 2000, message = "Massimo 2000 caratteri")
      String content
  ) {}

  @ModelAttribute("form")
  public ReviewForm defaultForm() {
    // default: 5 stelle, testo vuoto
    return new ReviewForm(5, "");
  }

  // ===== Helpers auth =====
  private AppUser resolveUserFrom(Credentials cred) {
    String[] getters = {"getAppUser", "getUser", "getUtente", "getOwner"};
    for (String g : getters) {
      try {
        Method m = cred.getClass().getMethod(g);
        Object o = m.invoke(cred);
        if (o instanceof AppUser u) return u;
      } catch (NoSuchMethodException ignored) {
        // passa al prossimo nome
      } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
            "Errore utente da credenziali (" + g + ")", e);
      }
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
        "Utente associato alle credenziali non trovato");
  }

  private AppUser currentUser(Authentication auth) {
    if (auth == null || !auth.isAuthenticated())
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non autenticato");

    String principal = auth.getName(); // username o email del login
    Credentials cred = credentials.findByUsername(principal)
        .orElseGet(() -> credentials.findByEmail(principal)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non trovate")));
    return resolveUserFrom(cred);
  }

  // ===== Pagine =====

  // GET form nuova recensione (querystring)
  @GetMapping("/reviews/new")
  public String newReviewQuery(@RequestParam("productId") Long productId, Model model) {
    model.addAttribute("productId", productId);
    return "review"; // templates/review.html
  }

  // GET form nuova recensione (path)
  @GetMapping("/reviews/new/{productId}")
  public String newReviewPath(@PathVariable Long productId, Model model) {
    model.addAttribute("productId", productId);
    return "review";
  }

  // CREATE: validazione manuale + redirect alla pagina prodotto (senza 409)
  @PostMapping("/reviews/product/{productId}/create")
  public String create(Authentication authentication,
                       @PathVariable Long productId,
                       @RequestParam("stars") Integer stars,
                       @RequestParam("content") String content,
                       RedirectAttributes ra) {

    if (stars == null || stars < 1 || stars > 5 || content == null || content.trim().isEmpty()) {
      ra.addFlashAttribute("error", "Compila stelle (1–5) e il commento.");
      return "redirect:/reviews/new?productId=" + productId;
    }

    AppUser me = currentUser(authentication);
    Product p = products.findById(productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prodotto inesistente"));

    // se già recensito → redirect con messaggio, niente 409/Whitelabel
    if (reviews.findByAuthorIdAndProductId(me.getId(), productId).isPresent()) {
      ra.addFlashAttribute("error", "Hai già recensito questo prodotto");
      return "redirect:/products/" + productId;
    }

    try {
      Review r = new Review();
      r.setAuthor(me);
      r.setProduct(p);
      r.setStars(stars);
      r.setContent(content.trim());
      reviews.save(r);
      ra.addFlashAttribute("ok", "Recensione salvata.");
    } catch (DataIntegrityViolationException e) {
      // copre anche il vincolo unique author+product lato DB
      ra.addFlashAttribute("error", "Hai già recensito questo prodotto");
    }

    return "redirect:/products/" + productId;
  }

  // UPDATE: validazione manuale + redirect al profilo
  @PostMapping("/reviews/{reviewId}/update")
  public String update(Authentication authentication,
                       @PathVariable Long reviewId,
                       @RequestParam("stars") Integer stars,
                       @RequestParam("content") String content,
                       RedirectAttributes ra) {

    if (stars == null || stars < 1 || stars > 5 || content == null || content.trim().isEmpty()) {
      ra.addFlashAttribute("error", "Compila stelle (1–5) e il commento.");
      return "redirect:/profile/reviews";
    }

    AppUser me = currentUser(authentication);
    Review r = reviews.findById(reviewId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review inesistente"));

    if (!r.getAuthor().getId().equals(me.getId()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non puoi modificare recensioni altrui");

    r.setStars(stars);
    r.setContent(content.trim());
    reviews.save(r);

    ra.addFlashAttribute("ok", "Recensione aggiornata.");
    return "redirect:/profile/reviews";
  }

  // GET profilo: le mie recensioni
  @GetMapping("/profile/reviews")
  public String myReviews(Authentication authentication,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "5") int size,
                          Model model) {
    AppUser me = currentUser(authentication);
    Page<Review> pg = reviews.findByAuthorIdOrderByCreatedAtDesc(me.getId(), PageRequest.of(page, size));
    model.addAttribute("page", pg);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", Math.max(pg.getTotalPages(), 1));
    return "profile-reviews"; // templates/profile-reviews.html
  }

  // POST delete
  @PostMapping("/reviews/{reviewId}/delete")
  public String delete(Authentication authentication,
                       @PathVariable Long reviewId,
                       RedirectAttributes ra) {
    AppUser me = currentUser(authentication);
    Review r = reviews.findById(reviewId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review inesistente"));
    if (!r.getAuthor().getId().equals(me.getId()))
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non puoi cancellare recensioni altrui");

    reviews.delete(r);
    ra.addFlashAttribute("ok", "Recensione eliminata.");
    return "redirect:/profile/reviews";
  }
}
