package com.meraki.workerNode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserQuota {

    private int rateLimit;
    private int windowSeconds;
    private int concurrencyLimit;
    private int timeOut;

    public UserQuota() {
        this.rateLimit = 5;
        this.windowSeconds = 300;
        this.concurrencyLimit = 2;
        this.timeOut = 600;
    }

}