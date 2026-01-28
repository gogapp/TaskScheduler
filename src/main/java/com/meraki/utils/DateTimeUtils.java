package com.meraki.utils;


import org.springframework.scheduling.support.CronExpression;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateTimeUtils {

    /**
     * Utility classes should not have public constructors Utility classes, which
     * are collections of static members, are not meant to be instantiated. <br>
     * Even abstract utility classes, which can be extended, should not have public
     * constructors.<br>
     * Java adds an implicit public constructor to every class which does not define
     * at least one explicitly. <br>
     * Hence, at least one non-public constructor should be defined.
     */
    private DateTimeUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Instant currentTimestamp() {
        return Instant.now();
    }

    public static Instant getNextStartTime(Instant currentTime, String cron) {
        CronExpression cronExpression = CronExpression.parse(cron);
        ZonedDateTime now = currentTime.atZone(ZoneId.systemDefault());
        ZonedDateTime next = cronExpression.next(now);
        if (next == null) {
            throw new IllegalStateException("No next execution time for cron: " + cron);
        }
        return next.toInstant();
    }
}
