package com.example.lock.service;

import com.example.lock.repository.LockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class NamedLockStockService {
    private final StockService stockService;
    private final LockRepository lockRepository;

    public void decrease(Long id, Long quantity){
        lockRepository.executeWithLock(id, quantity, stockService::decreaseWithLock);
    }
}
