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

import static org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper.SnmpMapperImpl.DEFAULT;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.o.ran.oam.nf.oam.adopter.api.FaultFields;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.pojos.TrapsMappingConfiguration;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class FaultFieldsHandler {
    private static final String SNMP_FAULT = "SNMP_Fault";
    private static final String SNMP_UNKNOWN = "Unknown";

    public static FaultFields toFaultFields(final TrapsMappingConfiguration trapsDescription, final PDU pdu) {
        final FaultFields faultFields = new FaultFields();
        setMandatoryFields(faultFields, trapsDescription, pdu);
        setOptionalFields(faultFields, trapsDescription, pdu);
        return faultFields;
    }

    private static void setMandatoryFields(final FaultFields faultFields,
            final TrapsMappingConfiguration trapsDescription, final PDU pdu) {
        faultFields.setAlarmCondition(trapsDescription.getName());
        faultFields.setEventSeverity(FaultFields.EventSeverity.fromValue(trapsDescription.getOidEventSeverity()));
        faultFields.setEventSourceType(Optional.ofNullable(trapsDescription.getEventSourceType()).orElse(SNMP_UNKNOWN));
        faultFields.setFaultFieldsVersion(FaultFields.FaultFieldsVersion._4_0);
        final String descOid = trapsDescription.getOidSpecificProblemDesc();
        faultFields.setSpecificProblem(SNMP_FAULT);
        if (descOid != null && !DEFAULT.equals(descOid)) {
            final Variable desc = pdu.getVariable(new OID(descOid));
            faultFields.setSpecificProblem(desc == null ? SNMP_FAULT : desc.toString());
        }
        faultFields.setVfStatus(FaultFields.VfStatus.ACTIVE);
    }

    private static void setOptionalFields(final FaultFields faultFields,
            final TrapsMappingConfiguration trapsDescription, final PDU pdu) {
        final List<? extends VariableBinding> variables = pdu.getVariableBindings();
        final Map<String, String> map = variables.stream()
                .collect(Collectors.toMap(x -> x.getOid().toString(), x -> x.getVariable().toString()));

        faultFields.setAlarmAdditionalInformation(map.isEmpty() ? null : map);
        final String interfaceOid = trapsDescription.getOidAlarmInterfaceName();
        if (interfaceOid != null) {
            final Variable desc = pdu.getVariable(new OID(interfaceOid));
            faultFields.setAlarmInterfaceA(desc == null ? SNMP_FAULT : desc.toString());
        }
        final String eCategory = trapsDescription.getEventCategory();
        faultFields.setEventCategory(eCategory);
    }
}
