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

package org.o.ran.oam.nf.oam.adopter.snmp.manager.configurations;

import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.SnmpManager;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.api.TimeZoneOffsetService;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper.SnmpMapper;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.properties.SnmpManagerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class SnmpManagerConfig {
    private final SnmpManagerProperties snmpManagerProperties;
    private final SnmpMapper snmpMapper;
    private final VesEventNotifier vesEventNotifier;
    private final TimeZoneOffsetService timeZoneOffsetService;

    /**
     * Snmp Manager Configuration Constructor.
     */
    @Autowired
    public SnmpManagerConfig(final SnmpManagerProperties snmpManagerProperties, final SnmpMapper snmpMapper,
            final VesEventNotifier vesEventNotifier, final TimeZoneOffsetService timeZoneOffsetService) {
        this.snmpManagerProperties = snmpManagerProperties;
        this.snmpMapper = snmpMapper;
        this.vesEventNotifier = vesEventNotifier;
        this.timeZoneOffsetService = timeZoneOffsetService;
    }

    @Bean
    public SnmpManager getSnmpManagerService() {
        return new SnmpManager(snmpManagerProperties.getHost(), snmpManagerProperties.getPort(), snmpMapper,
                vesEventNotifier, timeZoneOffsetService);
    }
}
