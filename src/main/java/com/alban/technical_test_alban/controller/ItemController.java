package com.alban.technical_test_alban.controller;

import com.alban.technical_test_alban.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alban.technical_test_alban.dto.ItemDTO;
import com.alban.technical_test_alban.service.ItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
	 private final ItemService itemService;
	    
	    @GetMapping("/{id}")
	    public ResponseEntity<ApiResponse<ItemDTO>> getItem(@PathVariable Long id) {
	        ItemDTO item = itemService.getItem(id);

			return ResponseEntity.ok(
					ApiResponse.<ItemDTO>builder()
							.success(true)
							.message("Item found")
							.data(item)
							.build()
			);
	    }

		/**
		 * Get items with pagination
		 */
	    @GetMapping
	    public ResponseEntity<ApiResponse<Page<ItemDTO>>>  getAllItems(
	            @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size,
	            @RequestParam(defaultValue = "id") String sortBy,
	            @RequestParam(defaultValue = "ASC") String sortDirection,
	            @RequestParam(defaultValue = "false") boolean includeStock) {

	        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
	        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

	        Page<ItemDTO> items = itemService.getAllItems(pageable, includeStock);
			return ResponseEntity.ok(
					ApiResponse.<Page<ItemDTO>>builder()
							.success(true)
							.message("Items retrieved successfully")
							.data(items)
							.build()
			);
	    }
	    
	    @PostMapping
	    public ResponseEntity<ApiResponse<ItemDTO>> createItem(@Valid @RequestBody ItemDTO itemDTO) {
	        ItemDTO createdItem = itemService.createItem(itemDTO);
			return ResponseEntity.ok(
					ApiResponse.<ItemDTO>builder()
							.success(true)
							.message("Item created successfully")
							.data(createdItem)
							.build()
			);
	    }
	    
	    @PutMapping("/{id}")
	    public ResponseEntity<ApiResponse<ItemDTO>> updateItem(
	            @PathVariable Long id,
	            @Valid @RequestBody ItemDTO itemDTO) {
	        ItemDTO updatedItem = itemService.updateItem(id, itemDTO);
			return ResponseEntity.ok(
					ApiResponse.<ItemDTO>builder()
							.success(true)
							.message("Item updated successfully")
							.data(updatedItem)
							.build()
			);
	    }
	    
	    @DeleteMapping("/{id}")
	    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
	        itemService.deleteItem(id);
			return ResponseEntity.ok(
					ApiResponse.<Void>builder()
							.success(true)
							.message("Item deleted successfully")
							.data(null)
							.build()
			);
	    }
}
