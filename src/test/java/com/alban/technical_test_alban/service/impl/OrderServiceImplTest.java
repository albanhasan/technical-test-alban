package com.alban.technical_test_alban.service.impl;

import com.alban.technical_test_alban.dto.OrderDTO;
import com.alban.technical_test_alban.entity.Item;
import com.alban.technical_test_alban.entity.Order;
import com.alban.technical_test_alban.exception.InsufficientStockException;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import com.alban.technical_test_alban.repository.ItemRepository;
import com.alban.technical_test_alban.repository.OrderRepository;
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
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Item testItem;
    private Order testOrder;
    private OrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(new BigDecimal("100.00"));

        testOrder = new Order();
        testOrder.setOrderNo("1");
        testOrder.setItem(testItem);
        testOrder.setQty(10);
        testOrder.setPrice(new BigDecimal("1000.00"));

        testOrderDTO = new OrderDTO();
        testOrderDTO.setOrderNo("1");
        testOrderDTO.setItemId(1L);
        testOrderDTO.setItemName("Test Item");
        testOrderDTO.setQty(10);
        testOrderDTO.setPrice(new BigDecimal("1000.00"));
    }

    @Test
    void getOrder_WhenOrderExists_ShouldReturnOrderDTO() {
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        
        OrderDTO result = orderService.getOrder(1L);

        
        assertNotNull(result);
        assertEquals("1", result.getOrderNo());
        assertEquals(1L, result.getItemId());
        assertEquals("Test Item", result.getItemName());
        assertEquals(10, result.getQty());
        assertEquals(new BigDecimal("1000.00"), result.getPrice());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrder_WhenOrderDoesNotExist_ShouldThrowResourceNotFoundException() {
        
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrder(1L));
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getAllOrders_ShouldReturnPageOfOrderDTOs() {
        
        Order order2 = new Order();
        order2.setOrderNo("1");
        order2.setItem(testItem);
        order2.setQty(5);
        order2.setPrice(new BigDecimal("500.00"));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder, order2));

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        
        Page<OrderDTO> result = orderService.getAllOrders(pageable);

        
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("1", result.getContent().get(0).getOrderNo());
        assertEquals("1", result.getContent().get(1).getOrderNo());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    void createOrder_WithSufficientStock_ShouldReturnCreatedOrderDTO() {
        
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.getRemainingStock(1L)).thenReturn(50); // Sufficient stock
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        
        OrderDTO result = orderService.createOrder(testOrderDTO);

        
        assertNotNull(result);
        assertEquals(1L, result.getItemId());
        assertEquals(10, result.getQty());
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowInsufficientStockException() {
        
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.getRemainingStock(1L)).thenReturn(5); // Insufficient stock

        
        InsufficientStockException exception = assertThrows(
            InsufficientStockException.class, 
            () -> orderService.createOrder(testOrderDTO)
        );
        
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        assertTrue(exception.getMessage().contains("Requested: 10"));
        assertTrue(exception.getMessage().contains("Available: 5"));
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithItemNotFound_ShouldThrowResourceNotFoundException() {
        
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(testOrderDTO));
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, never()).getRemainingStock(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrder_WithIncreasingQuantityAndSufficientStock_ShouldReturnUpdatedOrderDTO() {
        
        OrderDTO updatedDTO = new OrderDTO();
        updatedDTO.setItemId(1L);
        updatedDTO.setQty(15); // Increased from 10 to 15
        updatedDTO.setPrice(new BigDecimal("1500.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.getRemainingStock(1L)).thenReturn(10); // Sufficient for additional 5
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        
        OrderDTO result = orderService.updateOrder(1L, updatedDTO);

        
        assertNotNull(result);
        verify(orderRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrder_WithIncreasingQuantityButInsufficientStock_ShouldThrowInsufficientStockException() {
        
        OrderDTO updatedDTO = new OrderDTO();
        updatedDTO.setItemId(1L);
        updatedDTO.setQty(15); // Increased from 10 to 15
        updatedDTO.setPrice(new BigDecimal("1500.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.getRemainingStock(1L)).thenReturn(3); // Insufficient for additional 5

        
        InsufficientStockException exception = assertThrows(
            InsufficientStockException.class,
            () -> orderService.updateOrder(1L, updatedDTO)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        assertTrue(exception.getMessage().contains("Additional required: 5"));
        assertTrue(exception.getMessage().contains("Available: 3"));
        verify(orderRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, times(1)).getRemainingStock(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrder_WithDecreasingQuantity_ShouldNotCheckStock() {
        
        OrderDTO updatedDTO = new OrderDTO();
        updatedDTO.setItemId(1L);
        updatedDTO.setQty(5); // Decreased from 10 to 5
        updatedDTO.setPrice(new BigDecimal("500.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        
        OrderDTO result = orderService.updateOrder(1L, updatedDTO);

        
        assertNotNull(result);
        verify(orderRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, never()).getRemainingStock(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrder_WithSameQuantity_ShouldNotCheckStock() {
        
        OrderDTO updatedDTO = new OrderDTO();
        updatedDTO.setItemId(1L);
        updatedDTO.setQty(10); // Same as current
        updatedDTO.setPrice(new BigDecimal("1000.00"));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        
        OrderDTO result = orderService.updateOrder(1L, updatedDTO);

        
        assertNotNull(result);
        verify(orderRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(itemService, never()).getRemainingStock(anyLong());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrder_WhenOrderNotFound_ShouldThrowResourceNotFoundException() {
        
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(1L, testOrderDTO));
        verify(orderRepository, times(1)).findById(1L);
        verify(itemRepository, never()).findById(anyLong());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrder_WhenItemNotFound_ShouldThrowResourceNotFoundException() {
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> orderService.updateOrder(1L, testOrderDTO));
        verify(orderRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deleteOrder_WhenOrderExists_ShouldDeleteSuccessfully() {
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        
        orderService.deleteOrder(1L);

        
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    void deleteOrder_WhenOrderDoesNotExist_ShouldThrowResourceNotFoundException() {
        
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        
        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(1L));
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).delete(any(Order.class));
    }
}
