package com.example.blitzbuy.service.impl;

import com.example.blitzbuy.data.entity.Products;
import com.example.blitzbuy.repository.ProductRepository;
import com.example.blitzbuy.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CacheService cacheService;

    @Override
    public Products findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Products findByIdWithLock(Long id) {
        return productRepository.findByIdWithLock(id).orElse(null);
    }

    @Override
    public List<Products> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Products save(Products product) {
        Products savedProduct = productRepository.save(product);

        String key = "product_inventory:" + savedProduct.getId();
        cacheService.cacheString(key, String.valueOf(savedProduct.getInventoryCount()), 30, TimeUnit.MINUTES);

        return savedProduct;
    }
}
