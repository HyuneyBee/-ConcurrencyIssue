package com.example.lock.facade;

import com.example.lock.model.Stock;
import com.example.lock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedissonLockStockFacadeTest {
    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private RedissonLockStockFacade redissonLockStockFacade;

    @BeforeEach
    public void before() {
        Stock stock = new Stock(1L, 100L);
        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    public void delete() {
        stockRepository.deleteAll();
    }

    @Test
    public void lettuceLock100Request() throws InterruptedException {
        int threadCount = 100;
        // 쓰레드풀 할당
        ExecutorService executorService = Executors.newFixedThreadPool(32);

        // 쓰레드가 2개 이상일 경우 일정 개수의 쓰레드가 끝난 후 다음 쓰레드가 실행될 수 있도록 대기시키고 끝나면 다음 쓰레드가 실행
        CountDownLatch latch = new CountDownLatch(threadCount);

        IntStream.range(0, 100).forEach(i -> executorService.submit(() -> {
                    try {
                        redissonLockStockFacade.decrease(1L, 1L);
                    } finally {
                        latch.countDown();
                    }
                }
        ));

        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(0L);
    }
}