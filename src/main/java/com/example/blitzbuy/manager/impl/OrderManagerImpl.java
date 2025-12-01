package com.example.blitzbuy.manager.impl;

import com.example.blitzbuy.data.dto.OrderEvent;
import com.example.blitzbuy.data.entity.Orders;
import com.example.blitzbuy.data.enums.Status;
import com.example.blitzbuy.manager.OrderManager;
import com.example.blitzbuy.repository.OrdersRepository;
import com.example.blitzbuy.service.impl.CacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderManagerImpl implements OrderManager {

    private final OrdersRepository ordersRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    private final CacheService cacheService;

    @Override
    public Orders checkAndCreateOrders(Long productId, Long userId) {
        String key = "product_inventory:" + productId;
        Orders order = new Orders();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        String currentValue = cacheService.getCachedString(key);
        log.info("DEBUG: Product ID = {} | Redis Value Before Decr={}", productId, currentValue);
        Long newStockLevel = cacheService.decrementCounter(key);
        log.info("DEBUG: Product ID = {} | Redis Value After Decr={}", productId, newStockLevel);

            try {
                OrderEvent orderEvent = new OrderEvent();
                orderEvent.setProductId(productId);
                orderEvent.setUserId(userId);

                if(newStockLevel>=0L)
                    {
                        log.info("DEBUG: Product ID = {} | Inside the True loop with stock={}", productId, newStockLevel);
                        order.setStatus(Status.PROCESSING);
                        orderEvent.setStatus(Status.PROCESSING);
                    }
                else
                    {
                        order.setStatus(Status.FAILED);
                        orderEvent.setStatus(Status.FAILED);
                        log.info("DEBUG: Product ID = {} | Inside the False loop with stock={}", productId, newStockLevel);
                        cacheService.incrementCounter(key); // just to ensure that count remains at 0
                    }

        String orderJson = objectMapper.writeValueAsString(orderEvent);
        kafkaTemplate.send("orders_topic", orderJson);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return order;
    }
}
