package com.example.blitzbuy.data.dto;

import com.example.blitzbuy.data.enums.Status;
import lombok.Data;


@Data
public class OrderEvent {
    private Long userId;

    private Long productId;

    private Long orderId;

    private Status status;
}
