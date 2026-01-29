package com.meraki.workerNode.iface;

import com.meraki.workerNode.dto.QuotaAndRateLimitSummary;
import org.springframework.stereotype.Service;

@Service
public interface MonitoringService {

    QuotaAndRateLimitSummary getQuotasAndRateLimitSummary(String userId);

}
