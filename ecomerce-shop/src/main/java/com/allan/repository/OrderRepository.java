package com.allan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.allan.domain.OrderStatus;
import com.allan.model.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Eager-fetch orderItems + product + shippingAddress + user in a single
     * JOIN FETCH query. This eliminates the N+1 problem that would otherwise
     * fire when OrderMapper traverses order.getOrderItems().getProduct() etc.
     * Use these named queries everywhere the full DTO graph is needed.
     */
    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            LEFT JOIN FETCH o.shippingAddress
            LEFT JOIN FETCH o.user
            WHERE o.user.id = :userId
            """)
    List<Order> findByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            LEFT JOIN FETCH o.shippingAddress
            LEFT JOIN FETCH o.user
            WHERE o.sellerId = :sellerId
            """)
    List<Order> findBySellerId(@Param("sellerId") Long sellerId);

    // ── admin: by status ─────────────────────────────────────────────────────
    /**
     * This was the missing query — the bare findByOrderStatus() returned
     * uninitialized proxies for orderItems, causing the LazyInitializationError
     * on /api/admin/orders.
     */
    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            LEFT JOIN FETCH o.shippingAddress
            LEFT JOIN FETCH o.user
            WHERE o.orderStatus = :status
            """)
    List<Order> findByOrderStatus(@Param("status") OrderStatus status);


        long countByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);

        @Query("""
                SELECT COALESCE(SUM(o.totalSellingPrice), 0)
                FROM Order o
                WHERE o.user.id = :userId
                AND o.orderStatus = :orderStatus
                """)
        long sumTotalSellingPriceByUserIdAndOrderStatus(
                @Param("userId") Long userId,
                @Param("orderStatus") OrderStatus orderStatus);

    // ── admin: all orders ────────────────────────────────────────────────────
    /**
     * Override findAll() with a JOIN FETCH variant. Never call the inherited
     * JpaRepository.findAll() when the result will be mapped through OrderMapper.
     */
    @Query("""
            SELECT DISTINCT o FROM Order o
            LEFT JOIN FETCH o.orderItems oi
            LEFT JOIN FETCH oi.product
            LEFT JOIN FETCH o.shippingAddress
            LEFT JOIN FETCH o.user
            """)
    List<Order> findAllWithDetails();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi " +
       "LEFT JOIN FETCH oi.product p " +
       "LEFT JOIN FETCH p.seller " +
       "LEFT JOIN FETCH p.category " +
       "WHERE o.id = :id")
     Optional<Order> findByIdWithItemsAndProducts(@Param("id") Long id);
}

