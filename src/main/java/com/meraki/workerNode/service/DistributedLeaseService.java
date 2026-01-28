package com.meraki.workerNode.service;

import com.meraki.workerNode.dto.TaskMessage;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DistributedLeaseService {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    TaskExecutionService taskExecutionService;

    public void processWithTaskLease(TaskMessage taskMessage) {

        String lockKey = "task:lock:" + taskMessage.getTaskId();
        RLock lock = redissonClient.getLock(lockKey);

        lock.lock(); // watchdog auto-renewal enabled

        try {
            log.info("Lock acquired for task {}", taskMessage.getTaskId());
            taskExecutionService.executeTaskSync(taskMessage);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released for task {}", taskMessage.getTaskId());
            }
        }
    }
}