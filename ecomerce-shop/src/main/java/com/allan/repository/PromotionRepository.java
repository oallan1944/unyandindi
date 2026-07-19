package com.allan.repository;

import com.allan.domain.PromotionScope;
import com.allan.domain.PromotionStatus;
import com.allan.model.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Promotion}.
 *
 * <p><strong>Security notes:</strong>
 * <ul>
 *   <li>{@code sellerId == null} rows are platform-wide and admin-owned.
 *       {@code sellerId} references {@link com.allan.model.Seller#getId()}
 *       directly — there is no separate {@code Vendor} entity in this
 *       project. Any controller/service method that lets a seller mutate a
 *       promotion MUST resolve it via {@link #findByIdAndSellerId(Long, Long)}
 *       rather than {@link #findById(Long)} — this makes cross-tenant access
 *       (seller A editing seller B's promotion, or a seller editing a
 *       platform-wide promotion) impossible to express, instead of relying
 *       on an {@code if} check the service layer might forget.</li>
 *   <li>Admin-only endpoints may still use {@link #findById(Long)} directly.</li>
 *   <li>All queries are parameterized JPQL/derived queries — no string
 *       concatenation of caller input, so there is no injection surface here.</li>
 * </ul>
 */
@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    /**
     * Ownership-scoped lookup. Returns empty if the promotion doesn't exist
     * OR belongs to a different seller — callers should treat both cases as
     * "not found" (404), never leaking existence of another seller's promotion.
     */
    @Query("select p from Promotion p where p.id = :id and p.sellerId = :sellerId")
    Optional<Promotion> findByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    /** Paged listing for a seller's own dashboard. */
    @Query("select p from Promotion p where p.sellerId = :sellerId")
    Page<Promotion> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    /** Platform-wide promotions only (no owning seller) — admin view. */
    @Query("select p from Promotion p where p.sellerId is null")
    List<Promotion> findPlatformWide();

    /** Paged variant for admin dashboards. */
    @Query("select p from Promotion p where p.sellerId is null")
    Page<Promotion> findPlatformWide(Pageable pageable);

    List<Promotion> findByStatus(PromotionStatus status);

    Page<Promotion> findByStatus(PromotionStatus status, Pageable pageable);

    /**
     * Promotions of a given scope that are currently ACTIVE and within their
     * start/end window. Used by the evaluator at checkout time.
     *
     * <p>{@code now} is passed in by the caller (rather than using a DB
     * function) so evaluation time is deterministic and testable.
     */
    @Query("""
           select p from Promotion p
           where p.scope = :scope
             and p.status = com.allan.domain.PromotionStatus.ACTIVE
             and p.startsAt <= :now
             and p.endsAt >= :now
           order by p.priority asc
           """)
    List<Promotion> findActiveByScope(@Param("scope") PromotionScope scope,
                                       @Param("now") LocalDateTime now);

    /**
     * Active promotions for a specific seller (VENDOR_SPECIFIC scope),
     * ordered by priority so the evaluator can pick the lowest first.
     */
    @Query("""
           select p from Promotion p
           where p.sellerId = :sellerId
             and p.status = com.allan.domain.PromotionStatus.ACTIVE
             and p.startsAt <= :now
             and p.endsAt >= :now
           order by p.priority asc
           """)
    List<Promotion> findActiveBySellerId(@Param("sellerId") Long sellerId,
                                          @Param("now") LocalDateTime now);

    /**
     * Existence check used before allowing destructive edits in the service
     * layer (e.g. confirming a promotion truly belongs to the caller before
     * touching its rules/rewards).
     */
    @Query("select case when count(p) > 0 then true else false end " +
           "from Promotion p where p.id = :id and p.sellerId = :sellerId")
    boolean existsByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    /**
     * All ACTIVE promotions currently within their start/end window, across
     * every scope and seller. Backs {@code PromotionCacheService}'s flat
     * cache — unlike {@link #findByStatus(PromotionStatus)}, this also
     * excludes promotions whose {@code endsAt} has passed but haven't yet
     * been flipped to {@code EXPIRED} by a scheduler.
     */
    @Query("""
           select p from Promotion p
           where p.status = com.allan.domain.PromotionStatus.ACTIVE
             and p.startsAt <= :now
             and p.endsAt >= :now
           """)
    List<Promotion> findAllActiveNow(@Param("now") LocalDateTime now);
}