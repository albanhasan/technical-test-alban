package com.alban.technical_test_alban.controller;

import com.alban.technical_test_alban.dto.ItemDTO;
import com.alban.technical_test_alban.exception.GlobalExceptionHandler;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import com.alban.technical_test_alban.service.ItemService;
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

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ItemDTO testItemDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testItemDTO = new ItemDTO();
        testItemDTO.setId(1L);
        testItemDTO.setName("Pen");
        testItemDTO.setPrice(new BigDecimal("5.00"));
        testItemDTO.setRemainingStock(10);
    }

    @Test
    void getItem_WhenExists_ShouldReturn200() throws Exception {
        when(itemService.getItem(1L)).thenReturn(testItemDTO);

        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item found"))
                .andExpect(jsonPath("$.data.name").value("Pen"));

        verify(itemService, times(1)).getItem(1L);
    }

    @Test
    void getItem_WhenNotFound_ShouldReturn404() throws Exception {
        when(itemService.getItem(99L)).thenThrow(new ResourceNotFoundException("Item not found with id: 99"));

        mockMvc.perform(get("/items/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAllItems_ShouldReturn200WithPage() throws Exception {
        Page<ItemDTO> page = new PageImpl<>(Arrays.asList(testItemDTO), PageRequest.of(0, 10), 1);
        when(itemService.getAllItems(any(), anyBoolean())).thenReturn(page);

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Pen"));
    }

    @Test
    void createItem_WithValidData_ShouldReturn200() throws Exception {
        ItemDTO requestDTO = new ItemDTO();
        requestDTO.setName("Pen");
        requestDTO.setPrice(new BigDecimal("5.00"));

        when(itemService.createItem(any(ItemDTO.class))).thenReturn(testItemDTO);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item created successfully"));
    }

    @Test
    void createItem_WithMissingName_ShouldReturn400() throws Exception {
        ItemDTO invalidDTO = new ItemDTO();
        invalidDTO.setPrice(new BigDecimal("5.00"));
        // name is null/blank - should fail validation

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.name").exists());
    }

    @Test
    void createItem_WithMissingPrice_ShouldReturn400() throws Exception {
        ItemDTO invalidDTO = new ItemDTO();
        invalidDTO.setName("Pen");
        // price is null - should fail validation

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.price").exists());
    }

    @Test
    void updateItem_WithValidData_ShouldReturn200() throws Exception {
        ItemDTO requestDTO = new ItemDTO();
        requestDTO.setName("Pen Pro");
        requestDTO.setPrice(new BigDecimal("8.00"));

        when(itemService.updateItem(eq(1L), any(ItemDTO.class))).thenReturn(testItemDTO);

        mockMvc.perform(put("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item updated successfully"));
    }

    @Test
    void deleteItem_WhenExists_ShouldReturn200() throws Exception {
        doNothing().when(itemService).deleteItem(1L);

        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item deleted successfully"));

        verify(itemService, times(1)).deleteItem(1L);
    }
}
