package com.alban.technical_test_alban.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alban.technical_test_alban.entity.Inventory;

public interface OrderRepository extends JpaRepository<Inventory, Long> {

}
