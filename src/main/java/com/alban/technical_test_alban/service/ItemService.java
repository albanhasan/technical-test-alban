package com.alban.technical_test_alban.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.alban.technical_test_alban.dto.ItemDTO;

public interface ItemService {
	
	public ItemDTO getItem(Long id);
	 
	public Page<ItemDTO> getAllItems(Pageable pageable, boolean includeStock);
	
	public ItemDTO createItem(ItemDTO itemDTO);
	
	public ItemDTO updateItem(Long id, ItemDTO itemDTO);
	 
	public void deleteItem(Long id);
	
	public Integer getRemainingStock(Long itemId);
}
