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

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.HttpRestClient;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.mapper.PerformanceManagementFile2VesMapper;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.properties.PerformanceManagementManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerformanceManagementRestAgentFactory {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceManagementRestAgentFactory.class);

    private final VesEventNotifier eventListener;
    private final PerformanceManagementManagerProperties properties;
    private final PerformanceManagementFile2VesMapper pmFileMapper;
    private final HttpRestClient httpRestClient;

    /**
     * Default constructor.
     */
    public PerformanceManagementRestAgentFactory(final VesEventNotifier eventListener,
            final PerformanceManagementFile2VesMapper pmFileMapper,
            final PerformanceManagementManagerProperties properties, final HttpRestClient httpRestClient) {
        this.eventListener = eventListener;
        this.pmFileMapper = pmFileMapper;
        this.properties = properties;
        this.httpRestClient = httpRestClient;
    }

    /**
     * Generates new PM Agent which will get pm files via rest at specific time each day and
     * send it as CommonEventFormat302ONAP event via rest.
     * @param adapter IP address fo the adapter, adapter login username, adapter login password
     * @return PMRestAgent
     */
    public final Single<PerformanceManagementRestAgent> createPerformanceManagementRestAgent(final Adapter adapter) {
        return httpRestClient.getTimeZone(adapter).map(timeZone -> {
            final PerformanceManagementAgentRunnable pmAgentRunnable =
                    new PerformanceManagementAgentRunnable(httpRestClient, eventListener, pmFileMapper, adapter);
            return new PerformanceManagementRestAgent(pmAgentRunnable, properties.getSynchronizationTimeStart(),
                    properties.getSynchronizationTimeFrequency(), timeZone);
        });
    }

    private static class PerformanceManagementAgentRunnable implements Runnable {
        final HttpRestClient httpClient;
        private final VesEventNotifier pmEventListener;
        private final PerformanceManagementFile2VesMapper pmFileMapper;
        private final Adapter adapter;

        public PerformanceManagementAgentRunnable(final HttpRestClient httpClient,
                final VesEventNotifier pmEventListener,
                final PerformanceManagementFile2VesMapper pmFileMapper, final Adapter adapter) {
            this.httpClient = httpClient;
            this.pmEventListener = pmEventListener;
            this.pmFileMapper = pmFileMapper;
            this.adapter = adapter;
        }

        @Override
        public synchronized void run() {
            final String hostIp = adapter.getHostIpAddress();
            httpClient.readFiles(adapter)
                    .flatMapSingle(zip -> pmFileMapper.map(zip, hostIp))
                    .flatMapCompletable(events -> Observable.fromIterable(events)
                            .concatMapCompletable(pmEventListener::notifyEvents))
                    .doOnSubscribe(result -> LOG.info("PM VES notification forwarding for adapter {} started", hostIp))
                    .doOnComplete(() -> LOG.info("PM VES notification forwarding for adapter {} finished", hostIp))
                    .doOnError(error -> LOG.warn("PM VES notification forwarding for adapter {} failed", hostIp))
                    .subscribeOn(Schedulers.single())
                    .subscribe();
        }
    }
}
