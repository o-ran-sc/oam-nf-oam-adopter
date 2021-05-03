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

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.PerformanceManagementAdaptersDeployer;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.AlreadyPresentException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.NotFoundException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.mapper.PerformanceManagementFile2VesMapper;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.properties.PerformanceManagementManagerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {VesEventNotifierMock.class, PerformanceManagementMapperConfigProvider.class,
    PerformanceManagementFile2VesMapper.class, PerformanceManagementAdaptersDeployer.class})
public class PerformanceManagementManagerTest {

    @Autowired
    @Qualifier("test")
    private VesEventNotifierMock eventListener;
    @Autowired
    private PerformanceManagementFile2VesMapper fileMapper;
    private PerformanceManagementAdaptersDeployer deployer;

    /**
     * Initialize test.
     */
    @BeforeEach
    public void init() {
        final HttpRestClientMock httpRestClientMock = new HttpRestClientMock();
        final PerformanceManagementManagerProperties properties = new PerformanceManagementManagerProperties();
        final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("+02:00")).plusSeconds(5);
        final String formattedString = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        properties.setSynchronizationTimeStart(formattedString);
        properties.setSynchronizationTimeFrequency(30);
        deployer = new AdaptersDeployer(
                new PerformanceManagementRestAgentFactory(eventListener, fileMapper, properties, httpRestClientMock));
    }

    @Test
    @Timeout(value = 10000, unit = TimeUnit.MILLISECONDS)
    public void testMapping() throws IOException, InterruptedException, AlreadyPresentException {
        assertTrue(deployer.getAll().isEmpty());
        deployer.create("172.0.10.2", "admin", "admin");
        assertFalse(deployer.getAll().isEmpty());

        final String expected = JsonUtils.readJson("/json/PMVESMessage.json");
        final List<String> notifications = getVesNotification(eventListener, 2);
        final String actual = notifications.get(0);
        JsonUtils.compareResult(expected, actual);
    }

    @Test
    public void testDelete() throws AlreadyPresentException, NotFoundException {
        assertTrue(deployer.getAll().isEmpty());
        deployer.create("172.0.10.2", "admin", "admin");
        assertFalse(deployer.getAll().isEmpty());

        deployer.delete("172.0.10.2");
        assertTrue(deployer.getAll().isEmpty());
    }

    @Test
    public void testAlreadyPresent() throws AlreadyPresentException {
        assertTrue(deployer.getAll().isEmpty());
        deployer.create("172.0.10.2", "admin", "admin");
        assertFalse(deployer.getAll().isEmpty());

        final Exception alreadyPresentException = assertThrows(AlreadyPresentException.class,
                () -> deployer.create("172.0.10.2", "admin", "admin"));
        assertEquals(alreadyPresentException.getMessage(), "Adapter 172.0.10.2 already present.");
    }

    @Test
    public void testNotPresent() {
        final Exception exception = assertThrows(NotFoundException.class, () -> deployer.delete("172.0.10.2"));
        assertEquals(exception.getMessage(), "Adapter 172.0.10.2 is not present.");
    }

    @Test
    public void testTimeZone() throws AlreadyPresentException {
        deployer.create("172.0.10.2", "admin", "admin");
        assertEquals(deployer.getTimeZone("172.0.10.2"), ZoneId.of("+02:00"));
    }

    private static List<String> getVesNotification(final VesEventNotifierMock listener, final int expectedSize)
            throws InterruptedException {
        List<String> events = null;
        for (int i = 0; i < 100000; i++) {
            sleep(1000);
            events = listener.getEvents();
            if (events != null && !events.isEmpty() && events.size() == expectedSize) {
                break;
            }
        }
        return events;
    }

    @AfterEach
    public final void after() {
        ((AdaptersDeployer) deployer).close();
        assertTrue(deployer.getAll().isEmpty());
    }
}
