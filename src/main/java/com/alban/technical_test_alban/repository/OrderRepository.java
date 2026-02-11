package com.alban.technical_test_alban.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alban.technical_test_alban.entity.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(
            value = "SELECT 'O' || MAX(CAST(SUBSTRING(ORDER_NO, 2) AS INT)) FROM ORDERS",
            nativeQuery = true
    )
    String getLatestOrderNo();

    Optional<Order> findByOrderNo(String orderNo);
}
