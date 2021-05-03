/*
 *  ============LICENSE_START=======================================================
 *  O-RAN-SC
 *  ================================================================================
 *  Copyright © 2021 AT&T Intellectual Property. All rights reserved.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  ============LICENSE_END=========================================================
 */

package org.o.ran.oam.nf.oam.adopter.pm.rest.manager;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class PerformanceManagementRestAgent implements AutoCloseable {

    private static final DateTimeFormatter TIME_INPUT_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ScheduledExecutorService scheduler;
    private final Runnable pmAgent;
    private final LocalTime synchronizationTimeStart;
    private final int synchronizationTimeFrequency;
    private final ZoneId zoneId;


    PerformanceManagementRestAgent(final Runnable pmAgent, final String synchronizationTimeStart,
            final int synchronizationTimeFrequency, final ZoneId zoneId) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.pmAgent = pmAgent;
        this.synchronizationTimeStart = LocalTime.parse(synchronizationTimeStart, TIME_INPUT_FORMAT);
        this.synchronizationTimeFrequency = synchronizationTimeFrequency;
        this.zoneId = zoneId;
    }

    /**
     * Initialize service at fixed rate.
     */
    public void init() {
        final long initialDelay = initialDelay();
        scheduler.scheduleAtFixedRate(pmAgent, initialDelay, synchronizationTimeFrequency, TimeUnit.SECONDS);
    }

    private long initialDelay() {
        final ZonedDateTime now = ZonedDateTime.now(zoneId);

        ZonedDateTime nextRun = now
                .withHour(synchronizationTimeStart.getHour())
                .withMinute(synchronizationTimeStart.getMinute())
                .withSecond(synchronizationTimeStart.getSecond());
        if (now.compareTo(nextRun) > 0) {
            nextRun = nextRun.plusSeconds(synchronizationTimeFrequency);
        }
        final Duration duration = Duration.between(now, nextRun);
        return duration.getSeconds();
    }

    public ZoneId getTimeZone() {
        return zoneId;
    }

    @Override
    public void close() {
        scheduler.shutdown();
    }
}
