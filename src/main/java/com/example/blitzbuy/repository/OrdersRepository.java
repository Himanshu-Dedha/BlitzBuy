package com.example.blitzbuy.repository;

import com.example.blitzbuy.data.entity.Orders;
import com.example.blitzbuy.data.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    long countByStatus(Status status);
}
