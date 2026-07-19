package com.allan.service.support;

import java.time.Duration;

/**
 * Abstraction over a distributed lock, used by {@code RedemptionServiceImpl}
 * as the first layer of its three-layer redemption concurrency model (see
 * that class's javadoc): this lock, then a DB pessimistic write lock inside
 * the same transaction, then the unique constraint on
 * {@code coupon_redemptions.order_id} as the final guard.
 *
 * <p>Production implementation is Redis-backed
 * ({@code RedissonDistributedLockProvider}); the interface exists
 * independently of Redisson/Redis so {@code RedemptionServiceImpl} can be
 * unit-tested against a fake implementation without a real Redis instance.
 */
public interface DistributedLockProvider {

    /**
     * Attempts to acquire the lock for {@code key}, waiting up to
     * {@code waitTime} to acquire it, and holding it for at most
     * {@code leaseTime} once acquired (a TTL safety net against a crashed
     * holder — not a signal that normal callers should hold the lock that
     * long; release it explicitly and promptly via {@link LockHandle#close()}).
     *
     * @return a handle to release the lock, or {@code null} if it could not
     *         be acquired within {@code waitTime}. Callers must check for
     *         {@code null} — this is a fail-fast lock, not a blocking queue.
     */
    LockHandle tryLock(String key, Duration waitTime, Duration leaseTime);

    /**
     * Handle to a held lock. {@link #close()} releases it — always call from
     * a {@code finally} block (or try-with-resources) so a lock is never
     * held past the scope that acquired it, even on an exception path.
     */
    @FunctionalInterface
    interface LockHandle extends AutoCloseable {
        @Override
        void close();
    }
}