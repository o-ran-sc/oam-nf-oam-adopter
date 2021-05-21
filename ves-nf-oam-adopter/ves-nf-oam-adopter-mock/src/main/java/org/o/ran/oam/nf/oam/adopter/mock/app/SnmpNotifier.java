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

package org.o.ran.oam.nf.oam.adopter.mock.app;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.util.HashMap;
import org.o.ran.oam.nf.oam.adopter.mock.app.properties.SnmpProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class SnmpNotifier {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpNotifier.class);

    private final HashMap<String, String> alarmTrap;
    private final HashMap<String, String> clearTrap;
    private final CommunityTarget<UdpAddress> target;

    /**
     * Default constructor.
     */
    @Autowired
    public SnmpNotifier(final SnmpProperties snmpProperties) {
        this.alarmTrap = snmpProperties.getAlarmTrap();
        this.clearTrap = snmpProperties.getClearTrap();
        this.target = new CommunityTarget<>();
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(new UdpAddress(snmpProperties.getDestiny()));
    }

    @Scheduled(fixedDelayString = "${scheduler.fixedDelay}")
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private void sendAlarmTrap() throws IOException {
        sendTrap(target, alarmTrap, "alarm");
    }

    @Scheduled(fixedDelayString = "${scheduler.fixedDelay}", initialDelayString = "${scheduler.initialDelay}")
    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    private void sendClearTrap() throws IOException {
        sendTrap(target, clearTrap, "clear");
    }

    private static void sendTrap(final CommunityTarget<UdpAddress> target, final HashMap<String, String> trap,
            final String trapType) throws IOException {
        final PDU pdu = new PDU();
        pdu.setType(PDU.TRAP);
        trap.forEach((key, value) -> {
            try {
                pdu.add(new VariableBinding(new OID(key), new OctetString(value)));
            } catch (final Exception e) {
                LOG.error("Failed to parse oid / value : {}, {}", key, value, e);
            }
        });

        try (final Snmp snmp = new Snmp(new DefaultUdpTransportMapping())) {
            snmp.send(pdu, target, null, null);
            LOG.info("Trap {} sent successfully.", trapType);
        }
    }
}
