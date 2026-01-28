package com.meraki.workerNode.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcurrencyQuotaExceededException extends Exception {

    public ConcurrencyQuotaExceededException(String message, String logMessage, Object... logMessageArguments) {
        super(message);
        log.error(logMessage, logMessageArguments, this);
    }

}
