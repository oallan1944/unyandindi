package com.allan.service.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed {@link DistributedLockProvider}, via Redisson's {@link RLock}.
 *
 * <p>{@link RLock#isHeldByCurrentThread()} is checked before unlocking so a
 * thread never releases a lock it no longer owns — relevant if the lease
 * time was exceeded and Redisson's watchdog already let it expire under
 * sustained load, in which case a second unlock would either no-op safely
 * or, worse, release a lock a different thread has since acquired.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedissonDistributedLockProvider implements DistributedLockProvider {

    private final RedissonClient redissonClient;

    @Override
    public LockHandle tryLock(String key, Duration waitTime, Duration leaseTime) {
        RLock lock = redissonClient.getLock(key);

        boolean acquired;
        try {
            acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while acquiring distributed lock for key {}", key, e);
            return null;
        }

        if (!acquired) {
            log.warn("Failed to acquire distributed lock for key {} within {}", key, waitTime);
            return null;
        }

        return () -> {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        };
    }
}