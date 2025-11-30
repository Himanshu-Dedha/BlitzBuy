package com.example.blitzbuy.manager;

import com.example.blitzbuy.data.entity.Orders;

public interface OrderManager {
    Orders checkAndCreateOrders(Long productId, Long userId);
}
