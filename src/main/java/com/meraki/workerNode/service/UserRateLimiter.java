package com.meraki.workerNode.service;

import com.meraki.workerNode.dto.TaskMessage;
import com.meraki.workerNode.dto.UserQuota;
import com.meraki.workerNode.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.convert.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class UserRateLimiter {

    @Autowired
    ProxyManager<String> proxyManager;

    public void checkRateLimit(TaskMessage taskMessage) throws RateLimitExceededException {

        UserQuota quota = taskMessage.getUserQuota();

        BucketProxy bucket = proxyManager.builder()
                .build(userKey(taskMessage.getUserId()), () -> newBucket(quota));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Retry after " +
                            probe.getNanosToWaitForRefill() / 1_000_000 + " ms",
                    "Rate limit exceeded. Retry after " +
                            probe.getNanosToWaitForRefill() / 1_000_000 + " ms"
            );
        }
    }

    private BucketConfiguration newBucket(UserQuota quota) {
        return BucketConfiguration.builder()
                .addLimit(
                        Bandwidth.simple(
                                quota.getRateLimit(),
                                Duration.ofSeconds(quota.getWindowSeconds())
                        )
                )
                .build();
    }

    private String userKey(String userId) {
        return "rate:" + userId;
    }
}