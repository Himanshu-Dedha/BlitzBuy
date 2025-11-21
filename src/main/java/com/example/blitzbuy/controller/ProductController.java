package com.example.blitzbuy.controller;

import com.example.blitzbuy.data.entity.Products;
import com.example.blitzbuy.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequiredArgsConstructor
@RequestMapping("v1/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping()
    public ResponseEntity<Products> save(@RequestBody Products product) {
        return ResponseEntity.ok(productService.save(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Products> findProductById(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }
}
