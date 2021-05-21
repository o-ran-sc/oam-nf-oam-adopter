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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.api.TimeZoneOffsetService;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.configurations.SnmpManagerConfig;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper.SnmpMapperImpl;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.properties.SnmpManagerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {VesEventNotifierMock.class, SnmpMappingConfigurationProvider.class,
    SnmpMapperImpl.class, SnmpManagerProperties.class, SnmpManagerConfig.class})
class SnmpManagerTest {
    @Autowired
    @Qualifier("test")
    private VesEventNotifierMock listener;
    @Autowired
    private SnmpManagerProperties snmpManagerProperties;
    @Autowired
    private SnmpManagerImpl snmpManager;
    @MockBean
    private TimeZoneOffsetService timeZoneOffsetService;

    @BeforeEach
    public void init() {
        when(timeZoneOffsetService.getTimeZone(anyString())).thenReturn(ZoneId.of("+02:00"));
    }

    private String getVesNotification(final VesEventNotifierMock notificationProvider) throws InterruptedException {
        synchronized (notificationProvider) {
            for (int i = 0; i < 1000; i++) {
                notificationProvider.wait(200);
                if (notificationProvider.getEvent() != null) {
                    break;
                }
            }
        }
        final String event = notificationProvider.getEvent();
        assertNotNull(event);
        notificationProvider.clear();
        return event;
    }

    @Test
    void testDefaultTrap() throws Exception {
        SnmpTestUtil
                .sendDefaultTrapV2(snmpManagerProperties.getHost(), Integer.toString(snmpManagerProperties.getPort()));
        final String expected = JsonUtils.readJson("/json/VESMessageDefaultTrap.json");
        final String actual = getVesNotification(listener);
        JsonUtils.compareResultSkipEpoch(expected, actual);
    }

    @Test
    void testBoxDown() throws Exception {
        SnmpTestUtil.sendPortDownTrapV2(snmpManagerProperties.getHost(),
                Integer.toString(snmpManagerProperties.getPort()));
        final String expected = JsonUtils.readJson("/json/PortDOWN.json");
        final String actual = getVesNotification(listener);
        JsonUtils.compareResult(expected, actual);
    }
}
