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

import java.io.IOException;
import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.api.TimeZoneOffsetService;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper.SnmpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

final class SnmpTrapListener implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpTrapListener.class);
    private static final int THREADS_SIZE = 2;
    private final String hostPortAddress;
    private final SnmpCommandResponder commandResponder;

    public SnmpTrapListener(final String host, final Integer port, final SnmpMapper mapper,
            final VesEventNotifier vesEventNotifier, final TimeZoneOffsetService timeZoneOffsetService) {
        LOG.info("SnmpTrapListener listening on {}:{}", host, port);
        this.hostPortAddress = host + "/" + port;
        this.commandResponder = new SnmpCommandResponder(timeZoneOffsetService, mapper, vesEventNotifier);
    }

    @Override
    public synchronized void run() {
        try (final var snmpTarget = new DefaultUdpTransportMapping(new UdpAddress(hostPortAddress))) {
            final var threadPool = ThreadPool.create("SNMP_V2_Listener", THREADS_SIZE);
            final var dispatcher = new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
            dispatcher.addMessageProcessingModel(new MPv2c());
            listenSnmp(dispatcher, snmpTarget);
        } catch (final IOException e) {
            LOG.error("Error occurred while listening to SNMP messages: {}", e.getMessage());
        }
    }

    private synchronized void listenSnmp(final MessageDispatcher dispatcher,
            final DefaultUdpTransportMapping snmpTarget) {
        do {
            try (final var snmp = new Snmp(dispatcher, snmpTarget)) {
                snmp.addCommandResponder(commandResponder);
                snmpTarget.listen();
                LOG.debug("Listening on {}", snmpTarget);
                wait();
            } catch (final InterruptedException | IOException ex) {
                Thread.currentThread().interrupt();
            }
        } while (snmpTarget.isListening());
    }
}
