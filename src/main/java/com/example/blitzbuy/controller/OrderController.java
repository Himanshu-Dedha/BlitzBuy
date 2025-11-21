package com.example.blitzbuy.controller;

import com.example.blitzbuy.data.entity.Orders;
import com.example.blitzbuy.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/v1/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping()
    public ResponseEntity<Orders> generateOrders(@RequestParam(value = "userId") Long userId,
                                                 @RequestParam(value = "productId") Long productId) {
        return ResponseEntity.ok(orderService.checkAndCreateOrders(userId, productId));
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<String> findOrderStatus(@PathVariable(value = "orderId") Long orderId) {
        return ResponseEntity.ok("OKAY");
    }

}
