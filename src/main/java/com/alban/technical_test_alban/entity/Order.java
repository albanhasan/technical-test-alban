package com.alban.technical_test_alban.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ORDER_NO", nullable = false, unique = true)
    private String orderNo;

    @ManyToOne(optional = false)
    private Item item;

    @NotNull
    @Positive
    private Integer qty;

    @NotNull
    @Positive
    private BigDecimal price;
}
