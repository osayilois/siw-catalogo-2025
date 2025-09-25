package com.osayi.catalogo.repository;

import com.osayi.catalogo.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

  Page<Review> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

  Optional<Review> findByAuthorIdAndProductId(Long authorId, Long productId);
}
