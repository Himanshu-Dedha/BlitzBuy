package com.example.blitzbuy.service.impl;

import com.example.blitzbuy.data.entity.Orders;
import com.example.blitzbuy.data.entity.Products;
import com.example.blitzbuy.data.enums.Status;
import com.example.blitzbuy.repository.OrdersRepository;
import com.example.blitzbuy.service.OrderService;
import com.example.blitzbuy.service.ProductService;
import com.example.blitzbuy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrdersRepository ordersRepository;
    private final UserService userService;
    private final ProductService productService;

    @Override
    public Orders checkAndCreateOrders(Long userId, Long productId) {
        Products product = productService.findById(productId);
        if(Objects.isNull(product)){
            throw new RuntimeException("The product does not exist");
        }
        Orders orders = new Orders();
        orders.setUserId(userId);
        orders.setProductId(productId);
        if(product.getInventoryCount()>0){
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            reduceInventoryCount(product);
            orders.setStatus(Status.PROCESSED);
        }
        else{
            orders.setStatus(Status.FAILED);
        }
        orders.setCreatedAt(LocalDateTime.now());
        orders.setUpdatedAt(LocalDateTime.now());
        return ordersRepository.save(orders);
    }

    public void reduceInventoryCount(Products product){
        product.setInventoryCount(product.getInventoryCount() - 1);
        productService.save(product);
    }
}
