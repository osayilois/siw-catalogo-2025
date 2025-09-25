package com.osayi.catalogo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Entity
@Table(
  name = "reviews",
  uniqueConstraints = @UniqueConstraint(name = "uk_review_user_product",
    columnNames = {"author_id","product_id"})
)
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Relazione al prodotto recensito
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  // Autore della recensione (utente)
  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "author_id", nullable = false)
  private AppUser author;

  // Stelle 1..5
  @Min(1) @Max(5)
  @Column(nullable = false)
  private Integer stars;

  // Testo della recensione
  @NotBlank
  @Size(max = 2000)
  @Column(nullable = false, length = 2000)
  private String content;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  // GETTER & SETTER
  public Long getId() { return id; }

  public Product getProduct() { return product; }
  public void setProduct(Product product) { this.product = product; }

  public AppUser getAuthor() { return author; }
  public void setAuthor(AppUser author) { this.author = author; }

  public Integer getStars() { return stars; }
  public void setStars(Integer stars) { this.stars = stars; }

  public String getContent() { return content; }
  public void setContent(String content) { this.content = content; }

  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
