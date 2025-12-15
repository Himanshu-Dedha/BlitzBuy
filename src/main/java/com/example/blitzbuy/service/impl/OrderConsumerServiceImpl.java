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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderConsumerServiceImpl implements OrderConsumerService {

    private final OrdersRepository ordersRepository;

    private final CacheService cacheService;

    private final ObjectMapper objectMapper;

    // 1. Configure the Retry Logic
    @RetryableTopic(
            attempts = "3",                       // Try 3 times total (1 initial + 2 retries)
            backoff = @Backoff(delay = 1000, multiplier = 2.0), // Wait 1s, then 2s
            autoCreateTopics = "true",            // Create the retry topics automatically
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE
    )
    @KafkaListener(topics = "orders_topic", groupId = "blitzbuy_consumer_group")
    public void consumeOrders(String message){
        log.info("CONSUMER RECEIVED MESSAGE: {}", message);
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            Orders order = new Orders();
            order.setProductId(event.getProductId());
            order.setUserId(event.getUserId());
            order.setIdempotencyKey(event.getIdempotencyKey());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setStatus(event.getStatus() == Status.PROCESSING ? Status.PROCESSED : event.getStatus());
            try {
                ordersRepository.save(order);
            } catch(DataIntegrityViolationException ex){
                log.warn("Duplicate Order Event detected for Key: {}. Skipping.", event.getIdempotencyKey());
            }
            cacheService.cacheString("req_idempotency"+event.getIdempotencyKey(), order.getStatus().toString(), 1, TimeUnit.DAYS);
            log.info("CONSUMER SAVED ORDER TO DB: Product {}", event.getProductId());
        } catch (JsonProcessingException e) {
            log.error("CONSUMER ERROR: {}",e.getMessage());
            e.printStackTrace();
        }
    }

    // 2. Configure the "Give Up" Logic
    @DltHandler
    public void listenDLQ(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("ALERT: Order failed after max retries. Moved to DLQ: {}", topic);
        log.error("Payload: {}", message);

        // In a real production app, you would:
        // 1. Save this to a "failed_orders" table in Postgres (if DB is up).
        // 2. Or send a Slack notification to the dev team.
        // 3. For this project: Just logging is enough to prove the concept.
    }
}
