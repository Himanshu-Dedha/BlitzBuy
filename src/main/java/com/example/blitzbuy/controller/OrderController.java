package com.example.blitzbuy.controller;

import com.example.blitzbuy.data.entity.Orders;
import com.example.blitzbuy.manager.OrderManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController {

    private final OrderManager orderManager;
    @PostMapping()
    public ResponseEntity<Orders> generateOrders(
            @RequestHeader(value="Idempotency-Key") UUID idempotencyKey,
            @RequestParam(value = "userId") Long userId,
            @RequestParam(value = "productId") Long productId) {
        return ResponseEntity.ok(orderManager.checkAndCreateOrders(productId, userId, idempotencyKey ));
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<String> findOrderStatus(
            @PathVariable(value = "orderId") Long orderId) {
        return ResponseEntity.ok("OKAY");
    }

}
