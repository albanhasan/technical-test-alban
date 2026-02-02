package com.alban.technical_test_alban.service;

import com.alban.technical_test_alban.dto.InventoryDTO;
import com.alban.technical_test_alban.entity.Inventory;
import com.alban.technical_test_alban.entity.Item;
import com.alban.technical_test_alban.exception.InsufficientStockException;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    public InventoryDTO getInventory(Long id);

    public Page<InventoryDTO> getAllInventories(Pageable pageable);

    public InventoryDTO createInventory(InventoryDTO inventoryDTO);

    public InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO);

    public void deleteInventory(Long id);
}
