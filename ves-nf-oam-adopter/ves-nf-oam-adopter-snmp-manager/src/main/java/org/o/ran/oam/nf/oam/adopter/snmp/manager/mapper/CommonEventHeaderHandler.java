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

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.o.ran.oam.nf.oam.adopter.api.CommonEventHeader;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.pojos.TrapsMappingConfiguration;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.pojos.VesMappingConfiguration;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;

/**
 * Follows
 * https://docs.onap.org/projects/onap-vnfrqts-requirements/en/latest/Chapter8/ves7_1spec.html#datatype-commoneventheader
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CommonEventHeaderHandler {
    static CommonEventHeader toCommonEventHeader(final UdpAddress peerAddress,
            final VesMappingConfiguration vesMappingConfig, final TrapsMappingConfiguration mappingConfiguration,
            final PDU pdu, final String timeZone) {
        final var header = new CommonEventHeader();
        setMandatoryFields(header, peerAddress, vesMappingConfig, mappingConfiguration, pdu);
        setOptionalFields(header, vesMappingConfig, mappingConfiguration, pdu, timeZone);
        return header;
    }

    private static void setOptionalFields(final CommonEventHeader header,
            final VesMappingConfiguration vesMappingConfig, final TrapsMappingConfiguration mappingConfiguration,
            final PDU pdu, final String timeZone) {
        header.setNfVendorName(vesMappingConfig.getNfVendorName());
        final String oidEntityId = mappingConfiguration.getOidReportingEntityID();
        if (oidEntityId != null) {
            final var uuid = pdu.getVariable(new OID(oidEntityId));
            header.setReportingEntityId(uuid == null ? null : uuid.toString());
        }
        header.setNfNamingCode(null); //NOP
        header.setNfcNamingCode(null); //NOP
        if (timeZone != null) {
            header.setTimeZoneOffset(timeZone);
        }
    }

    private static void setMandatoryFields(final CommonEventHeader header, final UdpAddress peerAddress,
            final VesMappingConfiguration vesMappingConfig, final TrapsMappingConfiguration mappingConfiguration,
            final PDU pdu) {
        header.setDomain(CommonEventHeader.Domain.FAULT);

        final Optional<OID> eventIdOid = Optional.ofNullable(mappingConfiguration.getOidEventId()).map(OID::new);
        final Optional<Variable> eventIdValue = eventIdOid.map(pdu::getVariable);
        final String eventId = eventIdValue.map(Variable::toString).orElse(pdu.getRequestID().toString());
        header.setEventId(eventId);
        header.setEventName(
                CommonEventHeader.Domain.FAULT.name() + "_" + vesMappingConfig.getReportingEntityName() + "_"
                        + mappingConfiguration.getName());

        final String oidStartEpoch = mappingConfiguration.getEventStartEpochMicrosec();
        if (oidStartEpoch != null) {
            final var uuid = pdu.getVariable(new OID(oidStartEpoch));
            header.setStartEpochMicrosec(uuid == null ? null : Long.valueOf(uuid.toString()));
        } else {
            header.setStartEpochMicrosec(System.currentTimeMillis());
        }

        final String oidLastEpoch = mappingConfiguration.getEventLastEpochMicrosec();
        if (oidLastEpoch != null) {
            final var uuid = pdu.getVariable(new OID(oidLastEpoch));
            header.setLastEpochMicrosec(uuid == null ? null : Long.valueOf(uuid.toString()));
        } else {
            header.setLastEpochMicrosec(System.currentTimeMillis());
        }
        header.setPriority(CommonEventHeader.Priority.HIGH);
        header.setReportingEntityName(vesMappingConfig.getReportingEntityName());
        header.setReportingEntityId(vesMappingConfig.getReportingEntityId());
        header.setSequence(extractEventSequence(mappingConfiguration, pdu));
        final String oidSourceName = mappingConfiguration.getOidSourceName();
        if (oidSourceName != null) {
            final var sourceName = pdu.getVariable(new OID(oidSourceName));
            header.setSourceName(sourceName.toString());
        } else {
            header.setSourceName(peerAddress.getInetAddress().getHostAddress());
        }
        header.setVersion(CommonEventHeader.Version._4_0);
        header.setVesEventListenerVersion(Optional.ofNullable(vesMappingConfig.getVesEventListenerVersion())
                                                  .map(CommonEventHeader.VesEventListenerVersion::fromValue)
                                                  .orElse(CommonEventHeader.VesEventListenerVersion._7_1));
    }

    private static long extractEventSequence(final TrapsMappingConfiguration mappingConfiguration, final PDU pdu) {
        final Optional<String> optEventSequenceOid = Optional.ofNullable(mappingConfiguration.getOidEventSequence());
        if (optEventSequenceOid.isPresent()) {
            final String eventSequenceOid = optEventSequenceOid.get();
            if (!eventSequenceOid.contains(".")) {
                return Long.parseLong(eventSequenceOid);
            }
            final Optional<Variable> optValue = Optional.ofNullable(pdu.getVariable(new OID(eventSequenceOid)));
            if (optValue.isPresent()) {
                return optValue.get().toLong();
            }
        }
        return 0L;
    }
}
