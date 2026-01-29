package com.meraki.workerNode.service;

import com.meraki.workerNode.dto.TaskMessage;
import com.meraki.workerNode.dto.UserQuota;
import com.meraki.workerNode.exception.RateLimitExceededException;
import io.github.bucket4j.*;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserRateLimiter {

    @Autowired
    ProxyManager<String> proxyManager;

    Map<String, SimpleBucketListener> bucketListenerMap = new ConcurrentHashMap<>();

    public void checkRateLimit(TaskMessage taskMessage) throws RateLimitExceededException {

        UserQuota quota = taskMessage.getUserQuota();

        SimpleBucketListener bucketListener = bucketListenerMap.getOrDefault(taskMessage.getUserId(), new SimpleBucketListener());
        BucketProxy bucket = proxyManager.builder()
                .build(userKey(taskMessage.getUserId()), () -> newBucket(quota))
                .toListenable(bucketListener);

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

    public SimpleBucketListener getBucketListener(String userId) {
        return bucketListenerMap.getOrDefault(userId, new SimpleBucketListener());
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