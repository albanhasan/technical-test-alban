package com.alban.technical_test_alban.service.impl;

import com.alban.technical_test_alban.dto.InventoryDTO;
import com.alban.technical_test_alban.entity.Inventory;
import com.alban.technical_test_alban.entity.Item;
import com.alban.technical_test_alban.exception.InsufficientStockException;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import com.alban.technical_test_alban.repository.InventoryRepository;
import com.alban.technical_test_alban.repository.ItemRepository;
import com.alban.technical_test_alban.service.InventoryService;
import com.alban.technical_test_alban.service.ItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    public InventoryDTO getInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        return convertToDTO(inventory);
    }

    public Page<InventoryDTO> getAllInventories(Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findAll(pageable);
        return inventories.map(this::convertToDTO);
    }

    public InventoryDTO createInventory(InventoryDTO inventoryDTO) {
        Item item = itemRepository.findById(inventoryDTO.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + inventoryDTO.getItemId()));

        // Validate withdrawal doesn't cause negative stock
        if ("W".equals(inventoryDTO.getType())) {
            Integer currentStock = itemService.getRemainingStock(item.getId());
            if (currentStock < inventoryDTO.getQty()) {
                throw new InsufficientStockException(
                        "Cannot withdraw " + inventoryDTO.getQty() + " items. Current stock: " + currentStock
                );
            }
        }

        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setQty(inventoryDTO.getQty());
        inventory.setType(inventoryDTO.getType());

        Inventory savedInventory = inventoryRepository.save(inventory);
        return convertToDTO(savedInventory);
    }

    public InventoryDTO updateInventory(Long id, InventoryDTO inventoryDTO) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        Item item = itemRepository.findById(inventoryDTO.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + inventoryDTO.getItemId()));

        // Calculate stock impact of the update
        Integer oldImpact = "T".equals(inventory.getType()) ? inventory.getQty() : -inventory.getQty();
        Integer newImpact = "T".equals(inventoryDTO.getType()) ? inventoryDTO.getQty() : -inventoryDTO.getQty();
        Integer stockDifference = newImpact - oldImpact;

        // Check if update would cause negative stock
        Integer currentStock = itemService.getRemainingStock(item.getId());
        if (currentStock + stockDifference < 0) {
            throw new InsufficientStockException(
                    "Update would result in negative stock. Current stock: " + currentStock
            );
        }

        inventory.setItem(item);
        inventory.setQty(inventoryDTO.getQty());
        inventory.setType(inventoryDTO.getType());

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return convertToDTO(updatedInventory);
    }

    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));

        // Check if deletion would cause negative stock
        Integer impact = "T".equals(inventory.getType()) ? -inventory.getQty() : inventory.getQty();
        Integer currentStock = itemService.getRemainingStock(inventory.getItem().getId());

        if (currentStock + impact < 0) {
            throw new InsufficientStockException(
                    "Cannot delete inventory. Would result in negative stock. Current stock: " + currentStock
            );
        }

        inventoryRepository.delete(inventory);
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setItemId(inventory.getItem().getId());
        dto.setItemName(inventory.getItem().getName());
        dto.setQty(inventory.getQty());
        dto.setType(inventory.getType());
        return dto;
    }
}
