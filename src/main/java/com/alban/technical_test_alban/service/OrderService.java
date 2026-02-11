package com.alban.technical_test_alban.service;

import com.alban.technical_test_alban.dto.OrderDTO;
import com.alban.technical_test_alban.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

	public OrderDTO getOrder(Long orderId);

	public Page<OrderDTO> getAllOrders(Pageable pageable);

	public OrderDTO createOrder(OrderDTO orderDTO);

	public OrderDTO updateOrder(Long orderNo, OrderDTO orderDTO);

	public void deleteOrder(Long orderNo);
}
