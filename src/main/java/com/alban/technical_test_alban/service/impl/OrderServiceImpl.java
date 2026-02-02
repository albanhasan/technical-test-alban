package com.alban.technical_test_alban.service.impl;

import com.alban.technical_test_alban.entity.Item;
import com.alban.technical_test_alban.exception.InsufficientStockException;
import com.alban.technical_test_alban.exception.ResourceNotFoundException;
import com.alban.technical_test_alban.repository.ItemRepository;
import com.alban.technical_test_alban.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alban.technical_test_alban.dto.OrderDTO;
import com.alban.technical_test_alban.entity.Order;
import com.alban.technical_test_alban.repository.OrderRepository;
import com.alban.technical_test_alban.service.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    public OrderDTO getOrder(Long orderNo) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order no: " + orderNo));
        return convertToDTO(order);
    }

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToDTO);
    }

    public OrderDTO createOrder(OrderDTO orderDTO) {
        Item item = itemRepository.findById(orderDTO.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + orderDTO.getItemId()));

        // Check stock availability
        Integer remainingStock = itemService.getRemainingStock(item.getId());
        if (remainingStock < orderDTO.getQty()) {
            throw new InsufficientStockException(
                    "Insufficient stock for item '" + item.getName() + "'. " +
                            "Requested: " + orderDTO.getQty() + ", Available: " + remainingStock
            );
        }

        Order order = new Order();
        order.setItem(item);
        order.setQty(orderDTO.getQty());
        order.setPrice(orderDTO.getPrice());

        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    public OrderDTO updateOrder(Long orderNo, OrderDTO orderDTO) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order no: " + orderNo));

        Item item = itemRepository.findById(orderDTO.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + orderDTO.getItemId()));

        // Calculate stock impact of the update
        Integer oldQty = order.getQty();
        Integer newQty = orderDTO.getQty();
        Integer additionalQtyNeeded = newQty - oldQty;

        // If increasing quantity, check if stock is available
        if (additionalQtyNeeded > 0) {
            Integer remainingStock = itemService.getRemainingStock(item.getId());
            if (remainingStock < additionalQtyNeeded) {
                throw new InsufficientStockException(
                        "Insufficient stock for item '" + item.getName() + "'. " +
                                "Additional required: " + additionalQtyNeeded + ", Available: " + remainingStock
                );
            }
        }

        order.setItem(item);
        order.setQty(orderDTO.getQty());
        order.setPrice(orderDTO.getPrice());

        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    public void deleteOrder(Long orderNo) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order no: " + orderNo));
        orderRepository.delete(order);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderNo(order.getOrderNo());
        dto.setItemId(order.getItem().getId());
        dto.setItemName(order.getItem().getName());
        dto.setQty(order.getQty());
        dto.setPrice(order.getPrice());
        return dto;
    }
}
