package com.example.blitzbuy.manager.impl;

import com.example.blitzbuy.data.entity.Products;
import com.example.blitzbuy.manager.CacheSyncManager;
import com.example.blitzbuy.service.ProductService;
import com.example.blitzbuy.service.impl.CacheService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheSyncManagerImpl implements CacheSyncManager {

    private final CacheService cacheService;

    private final ProductService productService;

    @Override
    @PostConstruct
    public void cacheSync() {
        log.info("starting cache warm-up");
        List<Products> products = productService.getAllProducts();
        for (Products product : products) {
            String key = "product_inventory:" + product.getId();
            cacheService.cacheString(key, String.valueOf(product.getInventoryCount()), 30, TimeUnit.MINUTES);
        }
        log.info("cached {} products", products.size());
    }
}
