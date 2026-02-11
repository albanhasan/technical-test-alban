package com.alban.technical_test_alban.controller;

import com.alban.technical_test_alban.dto.OrderDTO;
import com.alban.technical_test_alban.exception.GlobalExceptionHandler;
import com.alban.technical_test_alban.exception.InsufficientStockException;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import com.alban.technical_test_alban.service.OrderService;
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
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private OrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testOrderDTO = new OrderDTO();
        testOrderDTO.setOrderNo("ORD-ABCD1234");
        testOrderDTO.setItemId(1L);
        testOrderDTO.setItemName("Pen");
        testOrderDTO.setQty(5);
        testOrderDTO.setPrice(new BigDecimal("25.00"));
    }

    @Test
    void getOrder_WhenExists_ShouldReturn200() throws Exception {
        when(orderService.getOrder(1L)).thenReturn(testOrderDTO);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNo").value("ORD-ABCD1234"))
                .andExpect(jsonPath("$.data.itemName").value("Pen"));
    }

    @Test
    void getOrder_WhenNotFound_ShouldReturn404() throws Exception {
        when(orderService.getOrder(99L)).thenThrow(new ResourceNotFoundException("Order not found with id: 99"));

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getAllOrders_ShouldReturn200WithPage() throws Exception {
        Page<OrderDTO> page = new PageImpl<>(Arrays.asList(testOrderDTO), PageRequest.of(0, 10), 1);
        when(orderService.getAllOrders(any())).thenReturn(page);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].orderNo").value("ORD-ABCD1234"));
    }

    @Test
    void createOrder_WithValidData_ShouldReturn200() throws Exception {
        OrderDTO requestDTO = new OrderDTO();
        requestDTO.setItemId(1L);
        requestDTO.setQty(5);
        requestDTO.setPrice(new BigDecimal("25.00"));

        when(orderService.createOrder(any(OrderDTO.class))).thenReturn(testOrderDTO);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.orderNo").value("ORD-ABCD1234"));
    }

    @Test
    void createOrder_WithMissingItemId_ShouldReturn400() throws Exception {
        OrderDTO invalidDTO = new OrderDTO();
        // itemId is null - should fail validation
        invalidDTO.setQty(5);
        invalidDTO.setPrice(new BigDecimal("25.00"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.itemId").exists());
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldReturn400() throws Exception {
        OrderDTO requestDTO = new OrderDTO();
        requestDTO.setItemId(1L);
        requestDTO.setQty(999);
        requestDTO.setPrice(new BigDecimal("4995.00"));

        when(orderService.createOrder(any(OrderDTO.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock. Requested: 999, Available: 5"));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateOrder_WithValidData_ShouldReturn200() throws Exception {
        OrderDTO requestDTO = new OrderDTO();
        requestDTO.setItemId(1L);
        requestDTO.setQty(10);
        requestDTO.setPrice(new BigDecimal("50.00"));

        when(orderService.updateOrder(eq(1L), any(OrderDTO.class))).thenReturn(testOrderDTO);

        mockMvc.perform(put("/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order updated successfully"));
    }

    @Test
    void deleteOrder_WhenExists_ShouldReturn200() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order deleted successfully"));

        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    void deleteOrder_WhenNotFound_ShouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Order not found with id: 99"))
                .when(orderService).deleteOrder(99L);

        mockMvc.perform(delete("/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
