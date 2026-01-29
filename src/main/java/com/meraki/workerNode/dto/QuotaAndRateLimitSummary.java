package com.meraki.workerNode.dto;

import io.github.bucket4j.SimpleBucketListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotaAndRateLimitSummary {

    private Long consumed;
    private Long rejected;
    private Long concurrentTasks;

    public QuotaAndRateLimitSummary(Long numTasks, SimpleBucketListener simpleBucketListener) {
        this.consumed = simpleBucketListener.getConsumed();
        this.rejected = simpleBucketListener.getRejected();
        this.concurrentTasks = numTasks;
    }

}
