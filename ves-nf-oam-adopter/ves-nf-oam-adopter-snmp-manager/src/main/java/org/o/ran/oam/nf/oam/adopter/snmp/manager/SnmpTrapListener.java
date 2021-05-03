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

import com.google.gson.Gson;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.api.TimeZoneOffsetService;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper.SnmpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.MultiThreadedMessageDispatcher;
import org.snmp4j.util.ThreadPool;

final class SnmpTrapListener implements CommandResponder, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpTrapListener.class);
    private static final int THREADS_SIZE = 2;
    private final String hostPortAddress;
    private final SnmpMapper mapper;
    private final VesEventNotifier vesEventNotifier;
    private final Gson gson = new Gson();
    private final TimeZoneOffsetService timeZoneOffsetService;

    public SnmpTrapListener(final String host, final Integer port, final SnmpMapper mapper,
            final VesEventNotifier vesEventNotifier, final TimeZoneOffsetService timeZoneOffsetService) {
        LOG.info("SnmpTrapListener listening on {}:{}", host, port);
        this.hostPortAddress = host + "/" + port;
        this.mapper = mapper;
        this.vesEventNotifier = vesEventNotifier;
        this.timeZoneOffsetService = timeZoneOffsetService;
    }

    @SuppressFBWarnings("WA_NOT_IN_LOOP")
    @Override
    public synchronized void run() {
        try {
            final UdpAddress udpAddress = new UdpAddress(hostPortAddress);
            final DefaultUdpTransportMapping snmpTarget = new DefaultUdpTransportMapping(udpAddress);
            final ThreadPool threadPool = ThreadPool.create("SNMP_V2_Listener", THREADS_SIZE);
            final MessageDispatcher dispatcher =
                    new MultiThreadedMessageDispatcher(threadPool, new MessageDispatcherImpl());
            dispatcher.addMessageProcessingModel(new MPv2c());

            final Snmp snmp = new Snmp(dispatcher, snmpTarget);
            snmp.addCommandResponder(this);

            snmpTarget.listen();
            LOG.debug("Listening on {}", snmpTarget);
            try {
                wait();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        } catch (final IOException e) {
            LOG.error("Error occurred while listening to SNMP messages: {}", e.getMessage());
        }
    }

    /**
     * This method will be called whenever a pdu is received on the given port
     * specified in the listen() method.
     */
    @Override
    public synchronized void processPdu(final CommandResponderEvent cmdRespEvent) {
        LOG.info("Received PDU");
        final PDU pdu = cmdRespEvent.getPDU();
        if (pdu == null) {
            LOG.warn("Ignoring PDU.");
            return;
        }

        final UdpAddress address = (UdpAddress) cmdRespEvent.getPeerAddress();
        final ZoneId optZoneId = timeZoneOffsetService.getTimeZone(address.getInetAddress().getHostAddress());
        final String timeZone = Optional.ofNullable(optZoneId)
            .map(zoneId -> "UTC" + LocalDateTime.now().atZone(zoneId).getOffset().toString()).orElse(null);

        mapper.toEvent(address, timeZone, pdu)
                .flatMapCompletable(vesEventNotifier::notifyEvents)
                .doOnSubscribe(result -> LOG.debug("SNMP Trap processing started"))
                .doOnComplete(() -> LOG.debug("SNMP Trap processed successfully"))
                .doOnError(error -> LOG.error("Failed to process SNMP Trap", error))
                .subscribe();
    }
}
