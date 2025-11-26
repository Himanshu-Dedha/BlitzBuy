package com.example.blitzbuy.repository;

import com.example.blitzbuy.data.entity.Products;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p from Products p where p.id = :id")
    Optional<Products> findByIdWithLock(@Param("id") Long id);
}
