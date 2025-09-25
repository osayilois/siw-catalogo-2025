package com.osayi.catalogo.model;

import jakarta.persistence.*;
import java.time.*;
import lombok.Data;

@Data
@Entity
public class Review {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional=false) private Product product;

  @Column(nullable=false, length=2000) private String text;
  @Column(nullable=false) private String authorUsername; // user basic auth
  @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();

}

