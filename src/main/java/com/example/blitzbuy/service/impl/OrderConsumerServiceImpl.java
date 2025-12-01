package com.example.blitzbuy.service.impl;

import com.example.blitzbuy.data.dto.OrderEvent;
import com.example.blitzbuy.data.entity.Orders;
import com.example.blitzbuy.data.enums.Status;
import com.example.blitzbuy.repository.OrdersRepository;
import com.example.blitzbuy.service.OrderConsumerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumerServiceImpl implements OrderConsumerService {

    private final OrdersRepository ordersRepository;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "orders_topic", groupId = "blitzbuy_consumer_group")
    public void consumeOrders(String message){
        log.info("CONSUMER RECEIVED MESSAGE: {}", message);
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            Orders order = new Orders();
            order.setProductId(event.getProductId());
            order.setUserId(event.getUserId());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setStatus(event.getStatus() == Status.PROCESSING ? Status.PROCESSED : event.getStatus());
            ordersRepository.save(order);
            log.info("CONSUMER SAVED ORDER TO DB: Product {}", event.getProductId());
        } catch (JsonProcessingException e) {
            log.error("CONSUMER ERROR: {}",e.getMessage());
            e.printStackTrace();
        }
    }
}
