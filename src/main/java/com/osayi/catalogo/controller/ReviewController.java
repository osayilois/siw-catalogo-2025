//-----------------------------------------------------------------------------------
// ------ CONTROLLER RECENSIONI PER CREARE/VISUALIZZARE/MODIFICARE/CANCELLARE -------

package com.osayi.catalogo.controller;

import com.osayi.catalogo.model.AppUser;
import com.osayi.catalogo.model.Product;
import com.osayi.catalogo.model.Review;
import com.osayi.catalogo.model.Credentials;
import com.osayi.catalogo.repository.CredentialsRepository;
import com.osayi.catalogo.repository.ProductRepository;
import com.osayi.catalogo.repository.ReviewRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/api")
public class ReviewController {

  private final ReviewRepository reviews;
  private final ProductRepository products;
  private final CredentialsRepository credentials;

  public ReviewController(ReviewRepository reviews, ProductRepository products, CredentialsRepository credentials) {
    this.reviews = reviews;
    this.products = products;
    this.credentials = credentials;
  }

  // === DTO minimi inline ===
  public record CreateReq(@Min(1) @Max(5) Integer stars,
                          @NotBlank @Size(max=2000) String content) {}
  public record UpdateReq(@Min(1) @Max(5) Integer stars,
                          @NotBlank @Size(max=2000) String content) {}
  public record ReviewDTO(Long id, Long productId, Long authorId, String authorName,
                          Integer stars, String content, String createdAt, String updatedAt) {}

  private ReviewDTO toDTO(Review r) {
    return new ReviewDTO(
      r.getId(),
      r.getProduct().getId(),
      r.getAuthor().getId(),
      r.getAuthor().getName(),   // se non è "name", cambia qui
      r.getStars(),
      r.getContent(),
      r.getCreatedAt().toString(),
      r.getUpdatedAt().toString()
    );
  }

  /** Prova a estrarre l'AppUser da Credentials usando vari possibili getter via reflection. */
  private AppUser resolveUserFrom(Credentials cred) {
    if (cred == null) return null;

    // Ordine di tentativi: aggiungi qui altri nomi se nel tuo progetto sono diversi
    String[] getters = { "getAppUser", "getUser", "getUtente", "getOwner" };

    for (String g : getters) {
      try {
        Method m = cred.getClass().getMethod(g);
        Object o = m.invoke(cred);
        if (o instanceof AppUser u) return u;
      } catch (NoSuchMethodException ignored) {
        // passa al prossimo nome
      } catch (Exception e) {
        // getter esiste ma ha fallito → interrompi con errore chiaro
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
          "Errore accedendo all'utente associato alle credenziali (" + g + ")", e);
      }
    }

    // Se arriviamo qui, nessun getter ha funzionato
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
      "Non riesco a trovare l'utente associato alle credenziali: aggiungi un getter (es. getAppUser() o getUser()).");
  }

  /** Ricava l'AppUser a partire dall'Authentication.getName() passando da Credentials. */
  private AppUser currentUser(Authentication auth) {
    if (auth == null || !auth.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Non autenticato");
    }
    String principal = auth.getName(); // username o email del login

    // Prova prima come username, poi come email (se non hai email, elimina il secondo tentativo)
    Credentials cred = credentials.findByUsername(principal)
        .orElseGet(() -> credentials.findByEmail(principal)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenziali non trovate"))
        );

    AppUser user = resolveUserFrom(cred);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente associato non trovato");
    }
    return user;
  }

  // 1) PUBBLICO: lista review di un prodotto (paginata)
  @GetMapping("/reviews/product/{productId}")
  public Page<ReviewDTO> listByProduct(@PathVariable Long productId,
                                       @RequestParam(defaultValue="0") int page,
                                       @RequestParam(defaultValue="10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return reviews.findByProductIdOrderByCreatedAtDesc(productId, pageable).map(this::toDTO);
  }

  // 2) PROFILO: le MIE recensioni (paginata)
  @GetMapping("/reviews/me")
  public Page<ReviewDTO> myReviews(Authentication authentication,
                                   @RequestParam(defaultValue="0") int page,
                                   @RequestParam(defaultValue="10") int size) {
    AppUser me = currentUser(authentication);
    Pageable pageable = PageRequest.of(page, size);
    return reviews.findByAuthorIdOrderByCreatedAtDesc(me.getId(), pageable).map(this::toDTO);
  }

  // 3) CREA (1 sola per prodotto/utente)
  @PostMapping("/reviews/product/{productId}")
  @ResponseStatus(HttpStatus.CREATED)
  public ReviewDTO create(Authentication authentication,
                          @PathVariable Long productId,
                          @Valid @RequestBody CreateReq req) {
    AppUser me = currentUser(authentication);
    Product p = products.findById(productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Prodotto inesistente"));

    reviews.findByAuthorIdAndProductId(me.getId(), productId).ifPresent(r -> {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Hai già recensito questo prodotto");
    });

    Review r = new Review();
    r.setAuthor(me);
    r.setProduct(p);
    r.setStars(req.stars());
    r.setContent(req.content());
    return toDTO(reviews.save(r));
  }

  // 4) UPDATE (solo autore)
  @PutMapping("/reviews/{reviewId}")
  public ReviewDTO update(Authentication authentication,
                          @PathVariable Long reviewId,
                          @Valid @RequestBody UpdateReq req) {
    AppUser me = currentUser(authentication);
    Review r = reviews.findById(reviewId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review inesistente"));

    if (!r.getAuthor().getId().equals(me.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non puoi modificare recensioni altrui");
    }
    r.setStars(req.stars());
    r.setContent(req.content());
    return toDTO(reviews.save(r));
  }

  // 5) DELETE (solo autore)
  @DeleteMapping("/reviews/{reviewId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication authentication, @PathVariable Long reviewId) {
    AppUser me = currentUser(authentication);
    Review r = reviews.findById(reviewId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review inesistente"));

    if (!r.getAuthor().getId().equals(me.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non puoi cancellare recensioni altrui");
    }
    reviews.delete(r);
  }
}
