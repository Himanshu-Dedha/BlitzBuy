package com.example.blitzbuy.service;

import com.example.blitzbuy.data.entity.Products;

public interface ProductService {
    Products findById(Long id);

    Products save(Products product);
}
