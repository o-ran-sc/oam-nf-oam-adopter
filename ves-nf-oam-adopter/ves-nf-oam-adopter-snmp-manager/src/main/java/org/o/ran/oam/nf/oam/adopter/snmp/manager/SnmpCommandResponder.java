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

import java.time.LocalDateTime;
import java.util.Optional;
import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.api.TimeZoneOffsetService;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper.SnmpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommandResponder;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.smi.UdpAddress;

class SnmpCommandResponder implements CommandResponder {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpCommandResponder.class);
    private final TimeZoneOffsetService timeZoneOffsetService;
    private final SnmpMapper mapper;
    private final VesEventNotifier vesEventNotifier;

    public SnmpCommandResponder(final TimeZoneOffsetService timeZoneOffsetService, final SnmpMapper mapper,
            final VesEventNotifier vesEventNotifier) {
        this.timeZoneOffsetService = timeZoneOffsetService;
        this.mapper = mapper;
        this.vesEventNotifier = vesEventNotifier;
    }

    /**
     * This method will be called whenever a pdu is received on the given port
     * specified in the listen() method.
     */
    @Override
    public synchronized void processPdu(final CommandResponderEvent cmdRespEvent) {
        LOG.info("Received PDU");
        final var pdu = cmdRespEvent.getPDU();
        if (pdu == null) {
            LOG.warn("Ignoring PDU.");
            return;
        }

        final UdpAddress address = (UdpAddress) cmdRespEvent.getPeerAddress();
        final var optZoneId = timeZoneOffsetService.getTimeZone(address.getInetAddress().getHostAddress());
        final String timeZone = Optional.ofNullable(optZoneId)
                                        .map(zoneId -> "UTC" + LocalDateTime.now().atZone(zoneId).getOffset()
                                                                       .toString()).orElse(null);

        mapper.toEvent(address, timeZone, pdu).flatMapCompletable(vesEventNotifier::notifyEvents)
                .doOnSubscribe(result -> LOG.debug("SNMP Trap processing started"))
                .doOnComplete(() -> LOG.debug("SNMP Trap processed successfully"))
                .doOnError(error -> LOG.error("Failed to process SNMP Trap", error)).subscribe();
    }
}
