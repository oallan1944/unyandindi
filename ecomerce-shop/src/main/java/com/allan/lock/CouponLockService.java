package com.allan.lock;

import com.allan.exceptions.CouponLockAcquisitionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis-backed short-lived lock guarding the redemption critical section:
 * re-validate → increment {@code Coupon.usedCount} → write
 * {@code CouponRedemption}.
 *
 * <p><strong>Second line of defense:</strong> {@code Coupon.version}
 * ({@code @Version}) is the final guarantee if Redis is unavailable or this
 * lock is bypassed — this lock exists to avoid wasted work and races under
 * normal contention, not as the sole correctness mechanism.
 *
 * <p><strong>Fail fast:</strong> this is a checkout-path lock, not a queue.
 * A near-zero wait time means a contended request fails immediately with
 * {@link CouponLockAcquisitionException} rather than blocking the request
 * thread — the client should retry, not the server hold the connection open.
 *
 * <p><strong>Lease time</strong> is a TTL safety net only: if the holder
 * crashes mid-critical-section, the lock self-expires rather than
 * deadlocking every future redemption of that coupon. Normal releases
 * happen explicitly in the {@code finally} block of {@link #executeWithLock}
 * well before the lease expires.
 *
 * <p>Requires {@code org.redisson:redisson-spring-boot-starter} on the
 * classpath, which auto-configures a {@link RedissonClient} bean from the
 * existing {@code spring.redis.*} / {@code spring.data.redis.*} properties.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CouponLockService {

    private static final String LOCK_KEY_PREFIX = "coupon-lock:";
    private static final long WAIT_TIME_MS = 200L;   // fail fast — checkout path, not a queue
    private static final long LEASE_TIME_MS = 5_000L; // TTL safety net against crashed holders

    private final RedissonClient redissonClient;

    /**
     * Runs {@code action} while holding the distributed lock for
     * {@code couponCode}, guaranteeing release regardless of outcome.
     *
     * @throws CouponLockAcquisitionException if the lock could not be
     *         acquired within {@link #WAIT_TIME_MS}, or the wait was interrupted
     */
    public <T> T executeWithLock(String couponCode, Supplier<T> action) {
        String key = lockKey(couponCode);
        RLock lock = redissonClient.getLock(key);

        boolean acquired;
        try {
            acquired = lock.tryLock(WAIT_TIME_MS, LEASE_TIME_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CouponLockAcquisitionException(couponCode, e);
        }

        if (!acquired) {
            log.warn("Failed to acquire redemption lock for coupon {} within {}ms", couponCode, WAIT_TIME_MS);
            throw new CouponLockAcquisitionException(couponCode);
        }

        try {
            return action.get();
        } finally {
            // isHeldByCurrentThread guards against unlocking a lock this
            // thread no longer owns (e.g. lease already expired under load).
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String lockKey(String couponCode) {
        String normalized = couponCode == null ? "" : couponCode.trim().toUpperCase();
        return LOCK_KEY_PREFIX + normalized;
    }
}