package com.example.blitzbuy.service;

import com.example.blitzbuy.data.entity.Products;

import java.util.List;

public interface ProductService {
    Products findById(Long id);

    Products findByIdWithLock(Long id);

    List<Products> getAllProducts();

    Products save(Products product);
}
