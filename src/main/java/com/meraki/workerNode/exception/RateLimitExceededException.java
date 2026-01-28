package com.meraki.workerNode.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RateLimitExceededException extends Exception {

    public RateLimitExceededException(String message, String logMessage, Object... logMessageArguments) {
        super(message);
        log.error(logMessage, logMessageArguments, this);
    }

}
