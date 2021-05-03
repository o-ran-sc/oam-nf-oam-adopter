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

import static org.eclipse.jdt.annotation.Checks.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.PerformanceManagementAdaptersDeployer;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.AlreadyPresentException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.NotFoundException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class AdaptersDeployer implements PerformanceManagementAdaptersDeployer, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AdaptersDeployer.class);

    private final PerformanceManagementRestAgentFactory pmRestAgentFactory;
    private final Map<String, PerformanceManagementRestAgent> adapters = new ConcurrentHashMap<>();

    @Autowired
    public AdaptersDeployer(final PerformanceManagementRestAgentFactory pmRestAgentFactory) {
        this.pmRestAgentFactory = pmRestAgentFactory;
    }

    @Override
    public synchronized void create(final String hostIpAddress, final String username, final String password)
            throws AlreadyPresentException {
        LOG.info("Create device PM adapter {}", hostIpAddress);
        if (adapters.get(hostIpAddress) != null) {
            throw new AlreadyPresentException(hostIpAddress);
        }
        final Adapter adapter =
                Adapter.builder().username(username).password(password).hostIpAddress(hostIpAddress).build();
        final PerformanceManagementRestAgent pmRestAgent =
                pmRestAgentFactory.createPerformanceManagementRestAgent(adapter).blockingGet();
        pmRestAgent.init();
        adapters.put(hostIpAddress, pmRestAgent);
    }

    @Override
    public synchronized void delete(final String host) throws NotFoundException {
        LOG.info("Adapter PM adapter removed {}", requireNonNull(host));
        final PerformanceManagementRestAgent adapter = adapters.remove(host);
        if (adapter == null) {
            throw new NotFoundException(host);
        }
        adapter.close();
    }

    @Override
    public List<String> getAll() {
        return ImmutableList.copyOf(adapters.keySet());
    }

    @Override
    public ZoneId getTimeZone(final String host) {
        LOG.debug("Read time zone for {}", host);
        return Optional.ofNullable(adapters.get(host)).map(
                PerformanceManagementRestAgent::getTimeZone).orElse(null);
    }

    @Override
    public synchronized void close() {
        for (final String host : ImmutableList.copyOf(adapters.keySet())) {
            try {
                delete(host);
            } catch (final Exception e) {
                LOG.warn("Failed to delete device PM adapter {}", host);
            }
        }
        adapters.clear();
    }
}
