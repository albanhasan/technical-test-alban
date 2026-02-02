package com.alban.technical_test_alban.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alban.technical_test_alban.dto.ItemDTO;
import com.alban.technical_test_alban.entity.Item;
import com.alban.technical_test_alban.exception.DuplicateResourceException;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import com.alban.technical_test_alban.repository.ItemRepository;
import com.alban.technical_test_alban.service.ItemService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService{
	private final ItemRepository itemRepository;
	    
    public ItemDTO getItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return convertToDTO(item, true);
    }
    
    public Page<ItemDTO> getAllItems(Pageable pageable, boolean includeStock) {
        Page<Item> items = itemRepository.findAll(pageable);
        return items.map(item -> convertToDTO(item, includeStock));
    }
    
    public ItemDTO createItem(ItemDTO itemDTO) {
        // Check for duplicate name
        if (itemRepository.findByName(itemDTO.getName()).isPresent()) {
            throw new DuplicateResourceException("Item with name '" + itemDTO.getName() + "' already exists");
        }
        
        Item item = new Item();
        item.setName(itemDTO.getName());
        item.setPrice(itemDTO.getPrice());
        
        Item savedItem = itemRepository.save(item);
        return convertToDTO(savedItem, false);
    }
    
    public ItemDTO updateItem(Long id, ItemDTO itemDTO) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        
        // Check if name is being changed and if new name already exists
        if (!item.getName().equals(itemDTO.getName())) {
            if (itemRepository.findByName(itemDTO.getName()).isPresent()) {
                throw new DuplicateResourceException("Item with name '" + itemDTO.getName() + "' already exists");
            }
        }
        
        item.setName(itemDTO.getName());
        item.setPrice(itemDTO.getPrice());
        
        Item updatedItem = itemRepository.save(item);
        return convertToDTO(updatedItem, false);
    }
    
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        itemRepository.delete(item);
    }
    
    public Integer getRemainingStock(Long itemId) {
        Integer inventoryStock = itemRepository.calculateInventoryStock(itemId);
        Integer orderedStock = itemRepository.calculateOrderedStock(itemId);
        
        if (inventoryStock == null) inventoryStock = 0;
        if (orderedStock == null) orderedStock = 0;
        
        return inventoryStock - orderedStock;
    }
    
    private ItemDTO convertToDTO(Item item, boolean includeStock) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setPrice(item.getPrice());
        
        if (includeStock) {
            dto.setRemainingStock(getRemainingStock(item.getId()));
        }
        
        return dto;
    }
}
