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

package org.o.ran.oam.nf.oam.adopter.pm.rest.manager.configurations;

import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.PerformanceManagementRestAgentFactory;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.HttpRestClient;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.mapper.PerformanceManagementFile2VesMapper;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.properties.PerformanceManagementManagerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class PerformanceManagementRestManagerConfig {
    private final PerformanceManagementManagerProperties pmManagerProperties;
    private final VesEventNotifier eventListener;
    private final PerformanceManagementFile2VesMapper pmFileMapper;

    /**
     * Default constructor.
     */
    @Autowired
    public PerformanceManagementRestManagerConfig(final VesEventNotifier eventListener,
            final PerformanceManagementFile2VesMapper pmFileMapper,
            final PerformanceManagementManagerProperties pmManagerProperties) {
        this.eventListener = eventListener;
        this.pmFileMapper = pmFileMapper;
        this.pmManagerProperties = pmManagerProperties;
    }

    @Bean
    PerformanceManagementRestAgentFactory getPerformanceManagementRestManagerFactoryService(
            final HttpRestClient httpRestClient) {
        return new PerformanceManagementRestAgentFactory(eventListener, pmFileMapper, pmManagerProperties,
                httpRestClient);
    }
}
