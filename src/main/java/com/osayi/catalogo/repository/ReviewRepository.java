package com.osayi.catalogo.repository;

import com.osayi.catalogo.model.Product;
import com.osayi.catalogo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  List<Review> findByProductOrderByCreatedAtDesc(Product product);
}

