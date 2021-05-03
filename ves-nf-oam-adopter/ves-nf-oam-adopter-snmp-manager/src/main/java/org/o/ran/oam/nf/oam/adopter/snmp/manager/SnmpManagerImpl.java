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

package org.o.ran.oam.nf.oam.adopter.snmp.manager;

import javax.annotation.PostConstruct;
import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.api.TimeZoneOffsetService;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper.SnmpMapper;

public class SnmpManagerImpl implements AutoCloseable {
    private static final String SNMP_MANAGER_THREAD = "SnmpManager";
    private final Thread snmpThread;

    /**
     * Default constructor.
     */
    public SnmpManagerImpl(final String host, final int port, final SnmpMapper mapper,
            final VesEventNotifier vesEventNotifier, final TimeZoneOffsetService timeZoneOffsetService) {
        final SnmpTrapListener trapListener =
                new SnmpTrapListener(host, port, mapper, vesEventNotifier, timeZoneOffsetService);
        this.snmpThread = new Thread(trapListener, SNMP_MANAGER_THREAD);
    }

    @PostConstruct
    public void init() {
        snmpThread.start();
    }

    @Override
    public void close() {
        snmpThread.interrupt();
    }
}
