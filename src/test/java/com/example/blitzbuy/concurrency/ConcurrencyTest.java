package com.example.blitzbuy.concurrency;

import com.example.blitzbuy.data.entity.Products;
import com.example.blitzbuy.data.enums.Status;
import com.example.blitzbuy.manager.OrderManager;
import com.example.blitzbuy.repository.OrdersRepository;
import com.example.blitzbuy.repository.ProductRepository;
import com.example.blitzbuy.service.OrderService;
import com.example.blitzbuy.service.ProductService;
import com.example.blitzbuy.service.impl.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SuppressWarnings("java:S2925")
@SpringBootTest
class ConcurrencyTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderManager orderManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private CacheService cacheService;


    @BeforeEach
    void setup() {
        ordersRepository.deleteAll();
        productRepository.deleteAll();
    }


    @Test
    void testConcurrency_OversellingRedis() throws InterruptedException{
        Products product = new Products();
        product.setInventoryCount(10L);
        product.setPrice(100.0);
        Products savedProduct = productService.save(product);

        int numberOfThreads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // CountDownLatch is like a starting gun. It ensures all threads start at the same time.
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // -------------------------------------------------
        // 2. ACT: Fire 20 requests simultaneously
        // -------------------------------------------------
        for(int i=0; i<numberOfThreads; i++){
            executorService.submit(() -> {
                try {
                    orderManager.checkAndCreateOrders( savedProduct.getId(), 1L);
                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to finish their work
        latch.await();
        Thread.sleep(10000);

        // -------------------------------------------------
        // 3. ASSERT: The Moment of Truth
        // -------------------------------------------------

        long successfulOrders = ordersRepository.countByStatus(Status.PROCESSED);
        long failedOrders = ordersRepository.countByStatus(Status.FAILED);
        long processingOrders = ordersRepository.countByStatus(Status.PROCESSING);
        long totalOrders = ordersRepository.count();
        long inventoryCount = Long.parseLong(cacheService.getCachedString("product_inventory:" + savedProduct.getId()));


        System.out.println("===============================================");
        System.out.println("Expected Inventory: 0");
        System.out.println("Actual Inventory:   " + inventoryCount);
        System.out.println("-----------------------------------------------");
        System.out.println("Expected Orders:    10 (Processed) + 10 (Failed)");
        System.out.println("Actual Orders:      " + totalOrders);
        System.out.println("===============================================");

        // This assertion will FAIL if your code is buggy (which it is)
        // If the inventory is negative, the test fails, proving the race condition.
        assertEquals(0L, inventoryCount, "Inventory should never be negative!");
        assertEquals(10L, failedOrders, "There should be 10 failed orders");
        assertEquals(10L, successfulOrders, "There should be 10 successful orders");
        assertEquals(0L, processingOrders, "Processing orders should be 0");



    }

    @Test
    void testConcurrency_Overselling() throws InterruptedException{

    // 1. Saving the product
        Products product = new Products();
        product.setInventoryCount(10L);
        product.setPrice(100.0);
        Products savedProduct = productRepository.save(product);


        // We will spawn 20 threads (Users) to try and buy the 10 items
        int numberOfThreads = 20;
         ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // CountDownLatch is like a starting gun. It ensures all threads start at the same time.
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // -------------------------------------------------
        // 2. ACT: Fire 20 requests simultaneously
        // -------------------------------------------------


        for(int i=0; i<numberOfThreads; i++){
            executorService.submit(() -> {
                try {
                    orderService.checkAndCreateOrders(1L, savedProduct.getId());
                }catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to finish their work
        latch.await();
        // -------------------------------------------------
        // 3. ASSERT: The Moment of Truth
        // -------------------------------------------------

        Products updatedProduct = productRepository.findById(savedProduct.getId()).orElseThrow();
        long totalOrders = ordersRepository.count();


        System.out.println("===============================================");
        System.out.println("Expected Inventory: 0");
        System.out.println("Actual Inventory:   " + updatedProduct.getInventoryCount());
        System.out.println("-----------------------------------------------");
        System.out.println("Expected Orders:    10 (Processed) + 10 (Failed)");
        System.out.println("Actual Orders:      " + totalOrders);
        System.out.println("===============================================");

        // This assertion will FAIL if your code is buggy (which it is)
        // If the inventory is negative, the test fails, proving the race condition.
        assertEquals(0L, updatedProduct.getInventoryCount(), "Inventory should never be negative!");

    }

}
