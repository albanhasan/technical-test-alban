package com.alban.technical_test_alban.repository;

import com.alban.technical_test_alban.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import com.alban.technical_test_alban.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

}
