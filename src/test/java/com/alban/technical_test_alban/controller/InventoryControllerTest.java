package com.alban.technical_test_alban.controller;

import com.alban.technical_test_alban.dto.InventoryDTO;
import com.alban.technical_test_alban.exception.GlobalExceptionHandler;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import com.alban.technical_test_alban.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private InventoryDTO testInventoryDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testInventoryDTO = new InventoryDTO();
        testInventoryDTO.setId(1L);
        testInventoryDTO.setItemId(1L);
        testInventoryDTO.setItemName("Pen");
        testInventoryDTO.setQty(10);
        testInventoryDTO.setType("T");
    }

    @Test
    void getInventory_WhenExists_ShouldReturn200() throws Exception {
        when(inventoryService.getInventory(1L)).thenReturn(testInventoryDTO);

        mockMvc.perform(get("/inventories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inventory found"))
                .andExpect(jsonPath("$.data.type").value("T"));
    }

    @Test
    void getInventory_WhenNotFound_ShouldReturn404() throws Exception {
        when(inventoryService.getInventory(99L))
                .thenThrow(new ResourceNotFoundException("Inventory not found with id: 99"));

        mockMvc.perform(get("/inventories/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAllInventories_ShouldReturn200WithPage() throws Exception {
        Page<InventoryDTO> page = new PageImpl<>(Arrays.asList(testInventoryDTO), PageRequest.of(0, 10), 1);
        when(inventoryService.getAllInventories(any())).thenReturn(page);

        mockMvc.perform(get("/inventories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].itemName").value("Pen"));
    }

    @Test
    void createInventory_WithValidData_ShouldReturn200() throws Exception {
        InventoryDTO requestDTO = new InventoryDTO();
        requestDTO.setItemId(1L);
        requestDTO.setQty(10);
        requestDTO.setType("T");

        when(inventoryService.createInventory(any(InventoryDTO.class))).thenReturn(testInventoryDTO);

        mockMvc.perform(post("/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inventory created successfully"));
    }

    @Test
    void createInventory_WithInvalidType_ShouldReturn400() throws Exception {
        InventoryDTO invalidDTO = new InventoryDTO();
        invalidDTO.setItemId(1L);
        invalidDTO.setQty(10);
        invalidDTO.setType("X"); // Invalid type - must be T or W

        mockMvc.perform(post("/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.type").exists());
    }

    @Test
    void createInventory_WithMissingItemId_ShouldReturn400() throws Exception {
        InventoryDTO invalidDTO = new InventoryDTO();
        // itemId is null
        invalidDTO.setQty(10);
        invalidDTO.setType("T");

        mockMvc.perform(post("/inventories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.itemId").exists());
    }

    @Test
    void updateInventory_WithValidData_ShouldReturn200() throws Exception {
        InventoryDTO requestDTO = new InventoryDTO();
        requestDTO.setItemId(1L);
        requestDTO.setQty(20);
        requestDTO.setType("T");

        when(inventoryService.updateInventory(eq(1L), any(InventoryDTO.class))).thenReturn(testInventoryDTO);

        mockMvc.perform(put("/inventories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inventory updated successfully"));
    }

    @Test
    void deleteInventory_WhenExists_ShouldReturn200() throws Exception {
        doNothing().when(inventoryService).deleteInventory(1L);

        mockMvc.perform(delete("/inventories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Inventory deleted successfully"));

        verify(inventoryService, times(1)).deleteInventory(1L);
    }
}
