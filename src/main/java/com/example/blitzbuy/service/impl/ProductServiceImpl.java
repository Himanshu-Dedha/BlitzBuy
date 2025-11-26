package com.example.blitzbuy.service.impl;

import com.example.blitzbuy.data.entity.Products;
import com.example.blitzbuy.repository.ProductRepository;
import com.example.blitzbuy.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    @Override
    public Products findById(Long id) {
        return productRepository.findByIdWithLock(id).orElse(null);
    }

    @Override
    public Products save(Products product) {
        return productRepository.save(product);
    }
}
