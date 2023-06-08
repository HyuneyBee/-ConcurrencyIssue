package com.example.lock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long productId;
    private Long quantity;

    public Stock(final Long id, final Long quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public void decrease(final Long quantity) {
        if (this.quantity - quantity < 0) {
            throw new RuntimeException("재고 부족");
        }
        this.quantity = this.quantity - quantity;
    }
}
