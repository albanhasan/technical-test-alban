package com.alban.technical_test_alban.controller;

import com.alban.technical_test_alban.dto.ApiResponse;
import com.alban.technical_test_alban.dto.InventoryDTO;
import com.alban.technical_test_alban.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDTO>> getInventory(@PathVariable Long id) {
        InventoryDTO inventory = inventoryService.getInventory(id);
        return ResponseEntity.ok(
                ApiResponse.<InventoryDTO>builder()
                        .success(true)
                        .message("Inventory found")
                        .data(inventory)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InventoryDTO>>> getAllInventories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<InventoryDTO> inventories = inventoryService.getAllInventories(pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<InventoryDTO>>builder()
                        .success(true)
                        .message("Inventory found")
                        .data(inventories)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryDTO>> createInventory(@Valid @RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO createdInventory = inventoryService.createInventory(inventoryDTO);
        return ResponseEntity.ok(
                ApiResponse.<InventoryDTO>builder()
                        .success(true)
                        .message("Inventory Created successfully")
                        .data(createdInventory)
                        .build()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryDTO>> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryDTO inventoryDTO) {
        InventoryDTO updatedInventory = inventoryService.updateInventory(id, inventoryDTO);
        return ResponseEntity.ok(
                ApiResponse.<InventoryDTO>builder()
                        .success(true)
                        .message("Inventory Created successfully")
                        .data(updatedInventory)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Inventory deleted successfully")
                        .data(null)
                        .build()
        );
    }
}
