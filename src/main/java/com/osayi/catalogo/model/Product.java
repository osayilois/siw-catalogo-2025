// src/main/java/com/osayi/catalogo/model/Product.java
package com.osayi.catalogo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "products")
public class Product {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  private String name;

  @Column(length = 2000)
  private String description;

  @NotNull
  @DecimalMin("0.00")
  @Column(precision = 10, scale = 2, nullable = false)
  private BigDecimal price;

  // categoria come STRING 
  @NotBlank
  private String category = "Altro";

  // URL immagine inserito dall’admin
  @Column(name = "image_url", length = 2048)
  private String imageUrl;
}
