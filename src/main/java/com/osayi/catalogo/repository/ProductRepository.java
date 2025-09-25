// src/main/java/com/osayi/catalogo/repository/ProductRepository.java
package com.osayi.catalogo.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.osayi.catalogo.model.Product;

public interface ProductRepository extends CrudRepository<Product, Long> {

  // Usato da AdminProductController.list() e ProductController.catalog()
  List<Product> findAllByOrderByIdDesc();

  // Home: "Appena aggiunti" (ultimi 8 per id desc)
  List<Product> findTop8ByOrderByIdDesc();

  // Search SOLO per nome (case-insensitive) ordinato per id desc
  List<Product> findByNameContainingIgnoreCaseOrderByIdDesc(String name);

  // ProductController.detail(): "simili" (stessa categoria, escludi quello aperto, ordina per id desc)
  List<Product> findTop4ByCategoryAndIdNotOrderByIdDesc(String category, Long id);

}



