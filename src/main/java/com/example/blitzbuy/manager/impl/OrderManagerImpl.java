package com.example.blitzbuy.manager.impl;

import com.example.blitzbuy.data.entity.Orders;
import com.example.blitzbuy.data.enums.Status;
import com.example.blitzbuy.manager.OrderManager;
import com.example.blitzbuy.repository.OrdersRepository;
import com.example.blitzbuy.service.impl.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderManagerImpl implements OrderManager {

    private final OrdersRepository ordersRepository;

    private final CacheService cacheService;

    @Override
    public Orders checkAndCreateOrders(Long productId, Long userId) {
        String key = "product_inventory:" + productId;
        Orders order = new Orders();
        order.setUserId(userId);
        order.setProductId(productId);
        Long newStockLevel = cacheService.decrementCounter(key);
        if(newStockLevel>=0L){
            order.setStatus(Status.PROCESSED);
        }else{
            order.setStatus(Status.FAILED);
            cacheService.incrementCounter(key); // just to ensure that count remains at 0
        }
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return ordersRepository.save(order);
    }
}
