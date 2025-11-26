package com.example.blitzbuy.service.impl;

import com.example.blitzbuy.data.entity.Orders;
import com.example.blitzbuy.data.entity.Products;
import com.example.blitzbuy.data.enums.Status;
import com.example.blitzbuy.repository.OrdersRepository;
import com.example.blitzbuy.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private ProductService productService; // Note: We mock the Service dependency, not just the repo, if your logic calls the service.

    @InjectMocks
    private OrderServiceImpl orderService; // This is the "System Under Test" (SUT)

    @Test
    void checkAndCreateOrders_InventoryAvailable_ReturnsProcessed() {
        // 1. ARRANGE
        Long userId = 100L;
        Long productId = 1L;

        Products mockProduct = new Products();
        mockProduct.setId(productId);
        mockProduct.setInventoryCount(10L);

        // Teach the mock how to behave
        when(productService.findById(productId)).thenReturn(mockProduct);
        // When save is called, just return the object passed to it
        when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. ACT
        Orders result = orderService.checkAndCreateOrders(userId, productId);

        // 3. ASSERT
        assertEquals(Status.PROCESSED, result.getStatus());
        assertEquals(9L, mockProduct.getInventoryCount());
        verify(ordersRepository, times(1)).save(any(Orders.class));
        verify(productService, times(1)).save(mockProduct);

    }

    @Test
    void checkAndCreateOrders_NoInventory_ReturnsFailed() {
        // ARRANGE
        Long userId = 100L;
        Long productId = 1L;

        Products mockProduct = new Products();
        mockProduct.setId(productId);
        mockProduct.setInventoryCount(0L);

        when(productService.findById(productId)).thenReturn(mockProduct);
        when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Orders result = orderService.checkAndCreateOrders(userId, productId);

        assertEquals(Status.FAILED, result.getStatus());
        assertEquals(0L, mockProduct.getInventoryCount());
        verify(ordersRepository, times(1)).save(any(Orders.class));


    }

    @Test
    void checkAndCreateOrders_ProductNotFound_ThrowsException() {
        // 1. ARRANGE
        Long userId = 1L;
        Long productId = 99L;
        when(productService.findById(productId)).thenReturn(null);

        // 2. ACT & ASSERT
        assertThrows(RuntimeException.class, () -> orderService.checkAndCreateOrders(userId, productId));

        // 3. VERIFY
        verify(ordersRepository, never()).save(any());
    }
}