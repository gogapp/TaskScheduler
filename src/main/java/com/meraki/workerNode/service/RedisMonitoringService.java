package com.meraki.workerNode.service;

import com.meraki.workerNode.dto.QuotaAndRateLimitSummary;
import com.meraki.workerNode.iface.MonitoringService;
import io.github.bucket4j.SimpleBucketListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisMonitoringService implements MonitoringService {

    @Autowired
    ConcurrencyQuotaService concurrencyQuotaService;

    @Autowired
    UserRateLimiter userRateLimiter;

    public QuotaAndRateLimitSummary getQuotasAndRateLimitSummary(String userId) {
        Long numTasks = concurrencyQuotaService.getNumConcurrentTasksPerUser(userId);
        SimpleBucketListener bucketListener = userRateLimiter.getBucketListener(userId);
        return new QuotaAndRateLimitSummary(numTasks, bucketListener);
    }

}
