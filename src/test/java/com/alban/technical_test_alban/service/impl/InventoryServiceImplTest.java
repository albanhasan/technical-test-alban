package com.alban.technical_test_alban.service.impl;

import com.alban.technical_test_alban.dto.InventoryDTO;
import com.alban.technical_test_alban.entity.Inventory;
import com.alban.technical_test_alban.entity.Item;
import com.alban.technical_test_alban.exception.InsufficientStockException;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import com.alban.technical_test_alban.repository.InventoryRepository;
import com.alban.technical_test_alban.repository.ItemRepository;
import com.alban.technical_test_alban.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Item testItem;
    private Inventory testInventory;
    private InventoryDTO testInventoryDTO;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(new BigDecimal("100.00"));

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setItem(testItem);
        testInventory.setQty(50);
        testInventory.setType("T"); // Top-up

        testInventoryDTO = new InventoryDTO();
        testInventoryDTO.setId(1L);
        testInventoryDTO.setItemId(1L);
        testInventoryDTO.setItemName("Test Item");
        testInventoryDTO.setQty(50);
        testInventoryDTO.setType("T");
    }

    @Test
    void getInventory_WhenInventoryExists_ShouldReturnInventoryDTO() {
        
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        // Act
        InventoryDTO result = inventoryService.getInventory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getItemId());
        assertEquals("Test Item", result.getItemName());
        assertEquals(50, result.getQty());
        assertEquals("T", result.getType());
        verify(inventoryRepository, times(1)).findById(1L);
    }

    @Test
    void getInventory_WhenInventoryDoesNotExist_ShouldThrowResourceNotFoundException() {
        
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventory(1L));
        verify(inventoryRepository, times(1)).findById(1L);
    }

    @Test
    void getAllInventories_ShouldReturnPageOfInventoryDTOs() {
        
        Inventory inventory2 = new Inventory();
        inventory2.setId(2L);
        inventory2.setItem(testItem);
        inventory2.setQty(30);
        inventory2.setType("W"); // Withdrawal

        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> inventoryPage = new PageImpl<>(Arrays.asList(testInventory, inventory2));

        when(inventoryRepository.findAll(pageable)).thenReturn(inventoryPage);

        // Act
        Page<InventoryDTO> result = inventoryService.getAllInventories(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("T", result.getContent().get(0).getType());
        assertEquals("W", result.getContent().get(1).getType());
        verify(inventoryRepository, times(1)).findAll(pageable);
    }

    @Test
    void createInventory_WithTopUpType_ShouldReturnCreatedInventoryDTO() {
        
        InventoryDTO topUpDTO = new InventoryDTO();
        topUpDTO.setItemId(1L);
        topUpDTO.setQty(100);
        topUpDTO.setType("T");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryDTO result = inventoryService.createInventory(topUpDTO);

        // Assert
        assertNotNull(result);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, never()).getRemainingStock(anyLong()); // No need to check stock for top-up
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void createInventory_WithWithdrawalTypeAndSufficientStock_ShouldReturnCreatedInventoryDTO() {
        
        InventoryDTO withdrawalDTO = new InventoryDTO();
        withdrawalDTO.setItemId(1L);
        withdrawalDTO.setQty(20);
        withdrawalDTO.setType("W");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.getRemainingStock(1L)).thenReturn(50); // Sufficient stock
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryDTO result = inventoryService.createInventory(withdrawalDTO);

        // Assert
        assertNotNull(result);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void createInventory_WithWithdrawalTypeButInsufficientStock_ShouldThrowInsufficientStockException() {
        
        InventoryDTO withdrawalDTO = new InventoryDTO();
        withdrawalDTO.setItemId(1L);
        withdrawalDTO.setQty(100);
        withdrawalDTO.setType("W");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.getRemainingStock(1L)).thenReturn(50); // Insufficient stock

        
        InsufficientStockException exception = assertThrows(
            InsufficientStockException.class,
            () -> inventoryService.createInventory(withdrawalDTO)
        );

        assertTrue(exception.getMessage().contains("Cannot withdraw 100 items"));
        assertTrue(exception.getMessage().contains("Current stock: 50"));
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void createInventory_WhenItemNotFound_ShouldThrowResourceNotFoundException() {
        
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> inventoryService.createInventory(testInventoryDTO));
        verify(itemRepository, times(1)).findById(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void updateInventory_FromTopUpToTopUp_WithPositiveImpact_ShouldReturnUpdatedInventoryDTO() {
        
        InventoryDTO updatedDTO = new InventoryDTO();
        updatedDTO.setItemId(1L);
        updatedDTO.setQty(100); // Increased from 50 to 100
        updatedDTO.setType("T"); // Still top-up

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.getRemainingStock(1L)).thenReturn(50); // Current stock
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryDTO result = inventoryService.updateInventory(1L, updatedDTO);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void updateInventory_FromTopUpToWithdrawal_ShouldValidateStock() {
        
        InventoryDTO updatedDTO = new InventoryDTO();
        updatedDTO.setItemId(1L);
        updatedDTO.setQty(30);
        updatedDTO.setType("W"); // Changed from top-up to withdrawal

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.getRemainingStock(1L)).thenReturn(100); // Sufficient stock
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryDTO result = inventoryService.updateInventory(1L, updatedDTO);

        // Assert
        assertNotNull(result);
        verify(inventoryRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void updateInventory_WithNegativeStockResult_ShouldThrowInsufficientStockException() {
        
        InventoryDTO updatedDTO = new InventoryDTO();
        updatedDTO.setItemId(1L);
        updatedDTO.setQty(200);
        updatedDTO.setType("W"); // Changed to withdrawal with large quantity

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.getRemainingStock(1L)).thenReturn(100); // Not enough for withdrawal

        
        InsufficientStockException exception = assertThrows(
            InsufficientStockException.class,
            () -> inventoryService.updateInventory(1L, updatedDTO)
        );

        assertTrue(exception.getMessage().contains("Update would result in negative stock"));
        verify(inventoryRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void updateInventory_WhenInventoryNotFound_ShouldThrowResourceNotFoundException() {
        
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.updateInventory(1L, testInventoryDTO));
        verify(inventoryRepository, times(1)).findById(1L);
        verify(itemRepository, never()).findById(anyLong());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void updateInventory_WhenItemNotFound_ShouldThrowResourceNotFoundException() {
        
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> inventoryService.updateInventory(1L, testInventoryDTO));
        verify(inventoryRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void deleteInventory_WithTopUpType_ShouldValidateStockDecrease() {
        
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemService.getRemainingStock(1L)).thenReturn(100); // Sufficient stock to remove top-up

        // Act
        inventoryService.deleteInventory(1L);

        // Assert
        verify(inventoryRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(inventoryRepository, times(1)).delete(testInventory);
    }

    @Test
    void deleteInventory_WithWithdrawalType_ShouldIncreaseStockCalculation() {
        
        testInventory.setType("W"); // Withdrawal type
        testInventory.setQty(30);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemService.getRemainingStock(1L)).thenReturn(20); // Stock will increase after deletion

        // Act
        inventoryService.deleteInventory(1L);

        // Assert
        verify(inventoryRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(inventoryRepository, times(1)).delete(testInventory);
    }

    @Test
    void deleteInventory_WhenDeletionCausesNegativeStock_ShouldThrowInsufficientStockException() {
        
        testInventory.setQty(100); // Large top-up
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemService.getRemainingStock(1L)).thenReturn(50); // Deleting will cause negative stock

        
        InsufficientStockException exception = assertThrows(
            InsufficientStockException.class,
            () -> inventoryService.deleteInventory(1L)
        );

        assertTrue(exception.getMessage().contains("Cannot delete inventory"));
        assertTrue(exception.getMessage().contains("Would result in negative stock"));
        verify(inventoryRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    void deleteInventory_WhenInventoryNotFound_ShouldThrowResourceNotFoundException() {
        
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> inventoryService.deleteInventory(1L));
        verify(inventoryRepository, times(1)).findById(1L);
        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    void deleteInventory_WithZeroStockImpact_ShouldDeleteSuccessfully() {
        
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemService.getRemainingStock(1L)).thenReturn(50); // Exactly matches the top-up quantity

        // Act
        inventoryService.deleteInventory(1L);

        // Assert
        verify(inventoryRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(inventoryRepository, times(1)).delete(testInventory);
    }
}
