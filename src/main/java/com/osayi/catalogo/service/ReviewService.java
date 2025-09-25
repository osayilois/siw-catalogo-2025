package com.osayi.catalogo.service;

import com.osayi.catalogo.model.Product;
import com.osayi.catalogo.model.Review;
import com.osayi.catalogo.repository.ProductRepository;
import com.osayi.catalogo.repository.ReviewRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ReviewService {
  private final ReviewRepository reviews;
  private final ProductRepository products;
  public ReviewService(ReviewRepository r, ProductRepository p){ this.reviews=r; this.products=p; }

  public List<Review> listByProduct(Long productId){
    Product pr = products.findById(productId).orElseThrow();
    return reviews.findByProductOrderByCreatedAtDesc(pr);
  }
  @Transactional
  public Review add(Long productId, String text, String author){
    Product pr = products.findById(productId).orElseThrow();
    Review r = new Review();
    r.setProduct(pr); r.setText(text); r.setAuthorUsername(author);
    return reviews.save(r);
  }
  @Transactional
  public Review update(Long reviewId, String newText, String username){
    Review r = reviews.findById(reviewId).orElseThrow();
    if(!r.getAuthorUsername().equals(username)) throw new AccessDeniedException("Puoi modificare solo i tuoi commenti");
    r.setText(newText); return r;
  }
  @Transactional
  public void delete(Long reviewId, String username){
    Review r = reviews.findById(reviewId).orElseThrow();
    if(!r.getAuthorUsername().equals(username)) throw new AccessDeniedException("Puoi cancellare solo i tuoi commenti");
    reviews.delete(r);
  }
}
