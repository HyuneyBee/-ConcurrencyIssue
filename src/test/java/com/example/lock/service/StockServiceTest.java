package com.example.lock.service;

import com.example.lock.config.LockDataSourceConfig;
import com.example.lock.model.Stock;
import com.example.lock.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(LockDataSourceConfig.class)
public class StockServiceTest {

    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private StockService stockService;
    @Autowired
    private PessimisticLockStockService pessimisticLockStockService;
    @Autowired
    private NamedLockStockService namedLockStockService;

    @BeforeEach
    public void before() {
        Stock stock = new Stock(1L, 100L);
        stockRepository.saveAndFlush(stock);
    }

    @Test
    public void concurrency100Request() throws InterruptedException {
        int threadCount = 100;
        // 쓰레드풀 할당
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 쓰레드가 2개 이상일 경우 일정 개수의 쓰레드가 끝난 후 다음 쓰레드가 실행될 수 있도록 대기시키고 끝나면 다음 쓰레드가 실행
        CountDownLatch latch = new CountDownLatch(threadCount);

        IntStream.range(0, 100).forEach(i -> executorService.submit(() -> {
                    try {
                        stockService.decrease(1L, 1L);
                    }
                    finally {
                        latch.countDown();
                    }
                }
        ));

        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(0L);
    }

    @Test
    public void syncConcurrency100Request() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        CountDownLatch latch = new CountDownLatch(threadCount);

        IntStream.range(0, 100).forEach(i -> executorService.submit(() -> {
                    try {
                        stockService.syncDecrease(1L, 1L);
                    }
                    finally {
                        latch.countDown();
                    }
                }
        ));

        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(0L);
    }

    @Test
    public void pessimisticLockConcurrency100Request() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        CountDownLatch latch = new CountDownLatch(threadCount);

        IntStream.range(0, 100).forEach(i -> executorService.submit(() -> {
                    try {
                        pessimisticLockStockService.decrease(1L, 1L);
                    }
                    finally {
                        latch.countDown();
                    }
                }
        ));

        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(0L);
    }

    @Test
    public void namedLockConcurrency100Request() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(10);

        CountDownLatch latch = new CountDownLatch(threadCount);

        IntStream.range(0, 100).forEach(i -> executorService.submit(() -> {
                    try {
                        namedLockStockService.decrease(1L, 1L);
                    }
                    finally {
                        latch.countDown();
                    }
                }
        ));

        latch.await();
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertThat(stock.getQuantity()).isEqualTo(0L);
    }
}
