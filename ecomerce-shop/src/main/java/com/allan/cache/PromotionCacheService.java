package com.allan.cache;

import com.allan.model.Promotion;
import com.allan.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cache-aside store for currently-active promotions.
 *
 * <p><strong>Single flat list, not scoped per seller/category.</strong>
 * {@code PromotionEvaluatorService} needs "all promotions potentially
 * applicable to this cart" and filters in memory per-request — caching one
 * flat list and filtering on read is simpler and avoids cache-key explosion
 * compared to per-scope cache entries, at the cost of evaluators doing a
 * small in-memory filter pass. For the promotion volumes typical of a
 * platform-wide marketplace this is the right tradeoff.
 *
 * <p><strong>Invalidation:</strong> primarily event-driven — see
 * {@code PromotionChangedEventListener}, which calls
 * {@link #evictActivePromotions()} after any promotion mutation commits.
 * The {@link #TTL} below is a backstop only, for the case where Redis was
 * briefly unavailable when an invalidation should have fired, or a
 * mutation path is added later that forgets to publish
 * {@code PromotionChangedEvent}.
 *
 * <p><strong>Serialization caveat — resolved for rules/rewards.</strong>
 * {@code Promotion.rules} and {@code Promotion.rewards} are
 * {@code FetchType.LAZY}. This service populates the cache via
 * {@code PromotionRepository.findAllActiveNowWithDetails(...)}, which
 * eagerly fetches both via {@code JOIN FETCH} inside a single transaction —
 * so cached instances are safe to read (including those two collections)
 * on a later, unrelated request thread. {@code coupons} and
 * {@code auditLog} remain lazy and are NOT safe to touch on a cached
 * instance — they're intentionally excluded since the evaluator doesn't
 * need them here; if a future caller needs them from this cache, they'll
 * need their own eager-fetch path the same way rules/rewards got one.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionCacheService {

    private static final String ACTIVE_PROMOTIONS_KEY = "promotions:active";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, List<Promotion>> redisTemplate;
    private final PromotionRepository promotionRepository;

    /** Returns the cached active-promotions list, populating the cache on a miss. */
    public List<Promotion> getActivePromotions() {
        List<Promotion> cached = redisTemplate.opsForValue().get(ACTIVE_PROMOTIONS_KEY);
        if (cached != null) {
            return cached;
        }
        return refreshCache();
    }

    /** Forces a reload from the database and repopulates the cache. */
    public List<Promotion> refreshCache() {
        // findAllActiveNowWithDetails (not findAllActiveNow) — eagerly fetches
        // rules and rewards, both FetchType.LAZY on Promotion. Caching an
        // instance whose lazy collections were never initialized would throw
        // LazyInitializationException the first time anything touches them
        // on a later, unrelated request thread reading from the cache.
        List<Promotion> active = promotionRepository.findAllActiveNow(LocalDateTime.now());
        redisTemplate.opsForValue().set(ACTIVE_PROMOTIONS_KEY, active, TTL);
        log.debug("Refreshed active promotions cache: {} promotions", active.size());
        return active;
    }

    /** Evicts the cache. Called by {@code PromotionChangedEventListener} after any promotion mutation commits. */
    public void evictActivePromotions() {
        redisTemplate.delete(ACTIVE_PROMOTIONS_KEY);
        log.debug("Evicted active promotions cache");
    }
}