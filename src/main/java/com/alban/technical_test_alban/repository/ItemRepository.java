package com.alban.technical_test_alban.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.alban.technical_test_alban.entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
	Optional<Item> findByName(String name);
	    
    @Query("SELECT COALESCE(SUM(CASE WHEN i.type = 'T' THEN i.qty ELSE -i.qty END), 0) " +
           "FROM Inventory i WHERE i.item.id = :itemId")
    Integer calculateInventoryStock(@Param("itemId") Long itemId);
    
    @Query("SELECT COALESCE(SUM(o.qty), 0) FROM Order o WHERE o.item.id = :itemId")
    Integer calculateOrderedStock(@Param("itemId") Long itemId);
}
