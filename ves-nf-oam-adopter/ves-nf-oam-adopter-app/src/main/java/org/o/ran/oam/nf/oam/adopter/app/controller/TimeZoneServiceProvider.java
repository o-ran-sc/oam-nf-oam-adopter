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

package org.o.ran.oam.nf.oam.adopter.app.controller;

import java.time.ZoneId;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.PerformanceManagementAdaptersDeployer;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.api.TimeZoneOffsetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TimeZoneServiceProvider implements TimeZoneOffsetService {

    private final PerformanceManagementAdaptersDeployer deployer;

    @Autowired
    public TimeZoneServiceProvider(final PerformanceManagementAdaptersDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public ZoneId getTimeZone(final String host) {
        return deployer.getTimeZone(host);
    }
}
