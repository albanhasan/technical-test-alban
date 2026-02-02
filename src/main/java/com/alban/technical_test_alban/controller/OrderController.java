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

import com.alban.technical_test_alban.dto.OrderDTO;
import com.alban.technical_test_alban.entity.Order;
import com.alban.technical_test_alban.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
	
	private final OrderService orderService;
	
	
	@GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(
                ApiResponse.<OrderDTO>builder()
                        .success(true)
                        .message("Order found")
                        .data(orderService.getOrder(orderId))
                        .build()
        );
    }
	
	@GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderNo") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(
                ApiResponse.<Page<OrderDTO>>builder()
                        .success(true)
                        .message("Orders retrieved successfully")
                        .data(orders)
                        .build()
        );
    }
	
	@PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.ok(
                ApiResponse.<OrderDTO>builder()
                        .success(true)
                        .message("Order Created successfully")
                        .data(createdOrder)
                        .build()
        );
    }
    
    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderDTO orderDTO) {
        OrderDTO updatedOrder = orderService.updateOrder(orderId, orderDTO);
        return ResponseEntity.ok(
                ApiResponse.<OrderDTO>builder()
                        .success(true)
                        .message("Order Updated successfully")
                        .data(updatedOrder)
                        .build()
        );
    }
    
    @DeleteMapping("/{orderNo}")
    public ResponseEntity<ApiResponse<OrderDTO>> deleteOrder(@PathVariable Long orderNo) {
        orderService.deleteOrder(orderNo);
        return ResponseEntity.ok(
                ApiResponse.<OrderDTO>builder()
                        .success(true)
                        .message("Order Deleted successfully")
                        .data(null)
                        .build()
        );
    }
}
