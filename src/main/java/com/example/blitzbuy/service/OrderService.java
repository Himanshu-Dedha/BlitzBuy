package com.example.blitzbuy.service;

import com.example.blitzbuy.data.entity.Orders;

public interface OrderService {
    Orders checkAndCreateOrders(Long userId, Long productId);
}
