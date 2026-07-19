package com.allan.repository;

import com.allan.model.PromotionAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link PromotionAudit}.
 *
 * <p><strong>Security note — append-only by construction:</strong> this
 * interface deliberately extends the base Spring Data
 * {@link org.springframework.data.repository.Repository} marker instead of
 * {@code JpaRepository}/{@code CrudRepository}. Those parents auto-expose
 * {@code deleteById}, {@code delete}, {@code deleteAll}, and update-via-save
 * semantics. By declaring only {@code save} and read methods explicitly,
 * there is no {@code delete(...)} method anywhere on this bean for a
 * controller, service, or future teammate to accidentally call — the
 * append-only contract described on {@link PromotionAudit} is enforced by
 * the compiler, not just by convention or a DB grant.
 *
 * <p>Production deployments should still restrict the application's DB role
 * to INSERT + SELECT on {@code promotion_audit} as defense in depth, per the
 * entity's javadoc.
 */
public interface PromotionAuditRepository
        extends org.springframework.data.repository.Repository<PromotionAudit, Long> {

    PromotionAudit save(PromotionAudit audit);

    Optional<PromotionAudit> findById(Long id);

    List<PromotionAudit> findByPromotionIdOrderByCreatedAtDesc(Long promotionId);

    Page<PromotionAudit> findByPromotionIdOrderByCreatedAtDesc(Long promotionId, Pageable pageable);

    List<PromotionAudit> findByActorOrderByCreatedAtDesc(String actor);

    @Query("select a from PromotionAudit a where a.action = :action order by a.createdAt desc")
    List<PromotionAudit> findByAction(@Param("action") String action, Pageable pageable);
}