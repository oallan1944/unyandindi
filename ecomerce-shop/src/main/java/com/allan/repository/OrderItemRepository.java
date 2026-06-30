package com.allan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.allan.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
