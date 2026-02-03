package com.alban.technical_test_alban.service.impl;

import com.alban.technical_test_alban.dto.ItemDTO;
import com.alban.technical_test_alban.entity.Item;
import com.alban.technical_test_alban.exception.DuplicateResourceException;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import com.alban.technical_test_alban.repository.ItemRepository;
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
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item testItem;
    private ItemDTO testItemDTO;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(new BigDecimal("100.00"));

        testItemDTO = new ItemDTO();
        testItemDTO.setId(1L);
        testItemDTO.setName("Test Item");
        testItemDTO.setPrice(new BigDecimal("100.00"));
    }

    @Test
    void getItem_WhenItemExists_ShouldReturnItemDTO() {
       
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.calculateInventoryStock(1L)).thenReturn(100);
        when(itemRepository.calculateOrderedStock(1L)).thenReturn(20);

       
        ItemDTO result = itemService.getItem(1L);

        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals(new BigDecimal("100.00"), result.getPrice());
        assertEquals(80, result.getRemainingStock());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void getItem_WhenItemDoesNotExist_ShouldThrowResourceNotFoundException() {
       
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> itemService.getItem(1L));
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void getAllItems_WithIncludeStock_ShouldReturnPageOfItemDTOs() {
       
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Test Item 2");
        item2.setPrice(new BigDecimal("200.00"));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(Arrays.asList(testItem, item2));

        when(itemRepository.findAll(pageable)).thenReturn(itemPage);
        when(itemRepository.calculateInventoryStock(anyLong())).thenReturn(100);
        when(itemRepository.calculateOrderedStock(anyLong())).thenReturn(20);

       
        Page<ItemDTO> result = itemService.getAllItems(pageable, true);

        
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(80, result.getContent().get(0).getRemainingStock());
        verify(itemRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllItems_WithoutIncludeStock_ShouldReturnPageOfItemDTOsWithoutStock() {
       
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(Arrays.asList(testItem));

        when(itemRepository.findAll(pageable)).thenReturn(itemPage);

       
        Page<ItemDTO> result = itemService.getAllItems(pageable, false);

        
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertNull(result.getContent().get(0).getRemainingStock());
        verify(itemRepository, times(1)).findAll(pageable);
        verify(itemRepository, never()).calculateInventoryStock(anyLong());
    }

    @Test
    void createItem_WithValidData_ShouldReturnCreatedItemDTO() {
       
        when(itemRepository.findByName("Test Item")).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

       
        ItemDTO result = itemService.createItem(testItemDTO);

        
        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals(new BigDecimal("100.00"), result.getPrice());
        verify(itemRepository, times(1)).findByName("Test Item");
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_WithDuplicateName_ShouldThrowDuplicateResourceException() {
       
        when(itemRepository.findByName("Test Item")).thenReturn(Optional.of(testItem));

        
        assertThrows(DuplicateResourceException.class, () -> itemService.createItem(testItemDTO));
        verify(itemRepository, times(1)).findByName("Test Item");
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_WithValidData_ShouldReturnUpdatedItemDTO() {
       
        ItemDTO updatedDTO = new ItemDTO();
        updatedDTO.setName("Updated Item");
        updatedDTO.setPrice(new BigDecimal("150.00"));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.findByName("Updated Item")).thenReturn(Optional.empty());
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

       
        ItemDTO result = itemService.updateItem(1L, updatedDTO);

        
        assertNotNull(result);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findByName("Updated Item");
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_WithSameName_ShouldNotCheckDuplicate() {
       
        ItemDTO updatedDTO = new ItemDTO();
        updatedDTO.setName("Test Item"); // Same as original
        updatedDTO.setPrice(new BigDecimal("150.00"));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

       
        ItemDTO result = itemService.updateItem(1L, updatedDTO);

        
        assertNotNull(result);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, never()).findByName(anyString());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_WhenItemNotFound_ShouldThrowResourceNotFoundException() {
       
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> itemService.updateItem(1L, testItemDTO));
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_WithDuplicateName_ShouldThrowDuplicateResourceException() {
       
        ItemDTO updatedDTO = new ItemDTO();
        updatedDTO.setName("Duplicate Item");
        updatedDTO.setPrice(new BigDecimal("150.00"));

        Item existingItem = new Item();
        existingItem.setId(2L);
        existingItem.setName("Duplicate Item");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.findByName("Duplicate Item")).thenReturn(Optional.of(existingItem));

        
        assertThrows(DuplicateResourceException.class, () -> itemService.updateItem(1L, updatedDTO));
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findByName("Duplicate Item");
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void deleteItem_WhenItemExists_ShouldDeleteSuccessfully() {
       
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

       
        itemService.deleteItem(1L);

        
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).delete(testItem);
    }

    @Test
    void deleteItem_WhenItemDoesNotExist_ShouldThrowResourceNotFoundException() {
       
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> itemService.deleteItem(1L));
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, never()).delete(any(Item.class));
    }

    @Test
    void getRemainingStock_WithBothInventoryAndOrders_ShouldReturnCorrectValue() {
       
        when(itemRepository.calculateInventoryStock(1L)).thenReturn(100);
        when(itemRepository.calculateOrderedStock(1L)).thenReturn(30);

       
        Integer result = itemService.getRemainingStock(1L);

        
        assertEquals(70, result);
        verify(itemRepository, times(1)).calculateInventoryStock(1L);
        verify(itemRepository, times(1)).calculateOrderedStock(1L);
    }

    @Test
    void getRemainingStock_WithNullInventory_ShouldReturnNegativeValue() {
       
        when(itemRepository.calculateInventoryStock(1L)).thenReturn(null);
        when(itemRepository.calculateOrderedStock(1L)).thenReturn(20);

       
        Integer result = itemService.getRemainingStock(1L);

        
        assertEquals(-20, result);
    }

    @Test
    void getRemainingStock_WithNullOrders_ShouldReturnInventoryValue() {
       
        when(itemRepository.calculateInventoryStock(1L)).thenReturn(100);
        when(itemRepository.calculateOrderedStock(1L)).thenReturn(null);

       
        Integer result = itemService.getRemainingStock(1L);

        
        assertEquals(100, result);
    }

    @Test
    void getRemainingStock_WithBothNull_ShouldReturnZero() {
       
        when(itemRepository.calculateInventoryStock(1L)).thenReturn(null);
        when(itemRepository.calculateOrderedStock(1L)).thenReturn(null);

       
        Integer result = itemService.getRemainingStock(1L);

        
        assertEquals(0, result);
    }
}
