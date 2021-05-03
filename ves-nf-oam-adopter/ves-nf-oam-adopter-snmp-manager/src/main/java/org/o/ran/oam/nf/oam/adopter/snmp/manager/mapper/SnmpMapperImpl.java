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

package org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.rxjava3.core.Maybe;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.o.ran.oam.nf.oam.adopter.api.CommonEventFormat302ONAP;
import org.o.ran.oam.nf.oam.adopter.api.Event;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.SnmpMappingConfigurationProvider;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.pojos.TrapsMappingConfiguration;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.pojos.VesMappingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SnmpMapperImpl implements SnmpMapper {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpMapperImpl.class);
    public static final String DEFAULT = "default";
    public static final OID SNMP_TRAP_OID = new OID("1.3.6.1.6.3.1.1.4.1.0");
    private final SnmpMappingConfigurationProvider snmpMappingConfigurationProvider;

    @Autowired
    public SnmpMapperImpl(final SnmpMappingConfigurationProvider snmpMappingConfigurationProvider) {
        this.snmpMappingConfigurationProvider = snmpMappingConfigurationProvider;
    }

    @Override
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    public Maybe<CommonEventFormat302ONAP> toEvent(final UdpAddress peerAddress, final String timeZone, final PDU pdu) {
        try {
            final int eventType = pdu.getType();
            if (pdu.getType() != PDU.NOTIFICATION) {
                LOG.warn("SNMP Event type {} not supported, ignoring event.", eventType);
                return Maybe.empty();
            }
            LOG.info("Starting Mapping of SNMP Event type {}.", eventType);
            LOG.trace("Pdu received {}.", pdu);
            final Event event = new Event();
            final String trapOidVariable = pdu.getVariable(SNMP_TRAP_OID).toString();
            final VesMappingConfiguration vesMappingConfig =
                    snmpMappingConfigurationProvider.getVesMappingConfiguration();
            final Map<String, TrapsMappingConfiguration> trapsDescriptions = vesMappingConfig.getTraps().stream()
                    .collect(Collectors.toMap(TrapsMappingConfiguration::getOid, trapsDescription -> trapsDescription));
            final TrapsMappingConfiguration trapsDescription =
                    Optional.ofNullable(trapsDescriptions.get(trapOidVariable)).orElse(trapsDescriptions.get(DEFAULT));
            event.setCommonEventHeader(CommonEventHeaderHandler.toCommonEventHeader(peerAddress, vesMappingConfig,
                    trapsDescription, pdu, timeZone));
            event.setFaultFields(FaultFieldsHandler.toFaultFields(trapsDescription, pdu));
            final CommonEventFormat302ONAP eventFormat = new CommonEventFormat302ONAP();
            eventFormat.setEvent(event);
            eventFormat.setEventList(null);
            LOG.info("Mapping of SNMP Event type {} finished.", eventType);
            return Maybe.just(eventFormat);
        } catch (final Exception e) {
            return Maybe.error(e);
        }
    }
}
