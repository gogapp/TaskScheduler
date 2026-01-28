package com.meraki.constants;

public class Enums {

    public enum TaskStatus {
        SCHEDULED, PROCESSING, COMPLETED, TIMED_OUT, FAILED, RATE_LIMITED, QUOTA_EXCEEDED
    }

    public enum JobStatus {
        CREATED, SCHEDULED, EXPIRED
    }
}
