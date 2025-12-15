package com.example.blitzbuy.manager;

import com.example.blitzbuy.data.entity.Orders;

import java.util.UUID;

public interface OrderManager {
    Orders checkAndCreateOrders(Long productId, Long userId, UUID idempotencyKey);
}
