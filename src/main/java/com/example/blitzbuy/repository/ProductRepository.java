package com.example.blitzbuy.repository;

import com.example.blitzbuy.data.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {
}
