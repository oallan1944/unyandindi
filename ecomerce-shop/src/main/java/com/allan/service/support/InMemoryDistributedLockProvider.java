package com.allan.service.support;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * In-JVM fallback {@link DistributedLockProvider}.
 *
 * <p><strong>NOT safe for production multi-instance deployment.</strong>
 * This only serializes redemptions within a single application process —
 * if you run more than one instance behind a load balancer, two instances
 * can each acquire "the same" lock independently and both proceed, which is
 * exactly the double-redemption scenario the lock exists to prevent. This
 * class is provided only so the module compiles and works correctly in a
 * local/single-instance dev environment; it is restricted to the
 * {@code dev} profile so it can never accidentally become the active bean
 * in production. Wire a real Redis-backed {@code DistributedLockProvider}
 * (e.g. via Redisson) for every other profile.
 */
@Component
@Profile("dev")
public class InMemoryDistributedLockProvider implements DistributedLockProvider {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public LockHandle tryLock(String key, Duration waitTime, Duration leaseTime) {
        ReentrantLock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        boolean acquired;
        try {
            acquired = lock.tryLock(waitTime.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        if (!acquired) {
            return null;
        }
        // Note: leaseTime auto-release isn't implemented in this simple
        // fallback — a real Redis lock (Redisson) handles TTL-based
        // auto-expiry natively. Don't rely on this class beyond local dev.
        return () -> lock.unlock();
    }
}