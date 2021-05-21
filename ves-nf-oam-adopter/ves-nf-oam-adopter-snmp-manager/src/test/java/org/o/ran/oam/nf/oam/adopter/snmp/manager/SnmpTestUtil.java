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
import lombok.experimental.UtilityClass;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

@UtilityClass
public class SnmpTestUtil {
    public static final OID PORT_DOWN_OID = new OID(new int[] {1, 3, 6, 1, 4, 1, 1007, 0, 0, 1, 0, 1});
    public static final OID BOX_NAME = new OID(new int[] {1, 3, 6, 1, 4, 1, 1007, 0, 0, 1, 0, 2});
    public static final OID NOTIFICATION_DESCRIPTION = new OID(new int[] {1, 3, 6, 1, 4, 1, 1007, 0, 0, 1, 0, 3});
    public static final OID START_EPOCH = new OID(new int[] {1, 3, 6, 1, 4, 1, 1007, 0, 0, 1, 0, 4});
    public static final OID LAST_EPOCH = new OID(new int[] {1, 3, 6, 1, 4, 1, 1007, 0, 0, 1, 0, 5});
    public static final OID BOX_SERIAL = new OID(new int[] {1, 3, 6, 1, 4, 1, 1007, 0, 0, 1, 0, 6});
    public static final OID NOTIFICATION_INTERFACE = new OID(new int[] {1, 3, 6, 1, 4, 1, 1007, 0, 0, 1, 0, 7});

    private static void sndTrap(final PDU trap, final String host, final String port) throws IOException {
        final UdpAddress targetaddress = new UdpAddress(host + "/" + port);
        final CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString("public"));
        target.setVersion(SnmpConstants.version2c);
        target.setAddress(targetaddress);

        final Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
        snmp.send(trap, target, null, null);
        snmp.close();
    }

    /**
     * Mock Default trap.
     */
    public static void sendDefaultTrapV2(final String host, final String port) throws IOException {
        final PDU trap = new PDU();
        trap.setType(PDU.TRAP);

        final OID oid = new OID("1.2.3.4.5");
        trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));
        trap.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
        trap.add(new VariableBinding(SnmpConstants.sysDescr, new OctetString("System Description")));

        // Add Payload
        final Variable var = new OctetString("some string");
        trap.add(new VariableBinding(oid, var));

        sndTrap(trap, host, port);
    }

    /**
     * Mock trap.
     */
    public static void sendPortDownTrapV2(final String host, final String port) throws IOException {
        final PDU trap = new PDU();
        trap.setType(PDU.TRAP);

        trap.add(new VariableBinding(SnmpConstants.snmpTrapOID, PORT_DOWN_OID));
        trap.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));

        // Add Payload
        trap.add(new VariableBinding(BOX_NAME, new OctetString("OAM-BOX")));
        trap.add(new VariableBinding(BOX_SERIAL, new OctetString("10283")));
        trap.add(new VariableBinding(NOTIFICATION_DESCRIPTION, new OctetString("Port DOWN")));
        trap.add(new VariableBinding(START_EPOCH, new OctetString("1613592976108380")));
        trap.add(new VariableBinding(LAST_EPOCH, new OctetString("1613592976108880")));
        trap.add(new VariableBinding(NOTIFICATION_INTERFACE, new OctetString("A0")));
        sndTrap(trap, host, port);
    }
}
