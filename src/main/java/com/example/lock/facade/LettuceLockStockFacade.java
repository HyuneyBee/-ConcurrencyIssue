package com.example.lock.facade;

import com.example.lock.repository.RedisLockRepository;
import com.example.lock.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        // lock 획득
        while (!redisLockRepository.lock(id)){
            Thread.sleep(100);
        }

        try {
            // 재고감소
            stockService.decrease(id, quantity);
        } finally {
            // lock 해제
            redisLockRepository.unlock(id);
        }
    }
}
