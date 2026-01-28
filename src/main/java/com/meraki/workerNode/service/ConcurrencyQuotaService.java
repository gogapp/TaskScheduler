package com.meraki.workerNode.service;

import com.meraki.workerNode.dto.TaskMessage;
import com.meraki.workerNode.dto.UserQuota;
import com.meraki.workerNode.exception.ConcurrencyQuotaExceededException;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ConcurrencyQuotaService {

    @Autowired
    RedissonClient redissonClient;

    public boolean tryAcquire(TaskMessage taskMessage) throws ConcurrencyQuotaExceededException {

        UserQuota quota = taskMessage.getUserQuota();
        int maxConcurrency = quota.getConcurrencyLimit();

        RAtomicLong counter = redissonClient.getAtomicLong(key(taskMessage.getUserId()));

        long current = counter.incrementAndGet();
        if (current == 1) {
            counter.expire(Duration.ofMinutes(15)); // safety TTL
        } else if(current <= 0) {
            current = 1;
            counter.set(1);
        }

        if (current > maxConcurrency) {
            counter.decrementAndGet();
            throw new ConcurrencyQuotaExceededException(
                    "Concurrency quota exceeded for userId " + taskMessage.getUserId(),
                    "Concurrency quota exceeded for userId " + taskMessage.getUserId()
            );
        }

        return true;
    }

    public void release(String userId) {
        RAtomicLong counter = redissonClient.getAtomicLong(key(userId));
        long remaining = counter.decrementAndGet();

        if (remaining <= 0) {
            counter.delete();
        }
    }

    private String key(String userId) {
        return "concurrency:" + userId;
    }
}
