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

package org.o.ran.oam.nf.oam.adopter.pm.rest.manager.mapper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.o.ran.oam.nf.oam.adopter.api.CommonEventHeader;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.CsvConfiguration;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.VesMappingConfiguration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class CommonEventHeaderHandler {
    private static final String PM_NOTIFICATIONS = "PM_Notification";

    static CommonEventHeader toCommonEventHeader(final VesMappingConfiguration config, final String hostIp,
            final CsvConfiguration csv, final Map<String, String> record, final int sequence) {
        final CommonEventHeader header = new CommonEventHeader();
        setMandatoryFields(config, hostIp, csv, header, record, sequence);
        setOptionalFields(config, header);
        return header;
    }

    private static void setOptionalFields(final VesMappingConfiguration config, final CommonEventHeader header) {
        header.setNfVendorName(Optional.ofNullable(config.getNfVendorName()).orElse(null));
        header.setReportingEntityId(config.getReportingEntityId());
        header.setNfNamingCode(null); //NOP
        header.setNfcNamingCode(null); //NOP
        header.setTimeZoneOffset(null); //NOP
    }

    private static void setMandatoryFields(final VesMappingConfiguration config, final String hostIp,
            final CsvConfiguration csv, final CommonEventHeader header, final Map<String, String> record,
            final int sequence) {
        header.setDomain(CommonEventHeader.Domain.MEASUREMENT);
        header.setEventName(CommonEventHeader.Domain.FAULT.name()
                + "_" + config.getReportingEntityName()
                + "_" + Optional.ofNullable(config.getEventName()).orElse(PM_NOTIFICATIONS));
        header.setStartEpochMicrosec(System.currentTimeMillis());
        header.setLastEpochMicrosec(System.currentTimeMillis());
        header.setPriority(CommonEventHeader.Priority.fromValue(config.getPriority()));
        header.setReportingEntityName(config.getReportingEntityName());
        header.setSequence((long) sequence);
        final String sourceNameField = csv.getSourceName();
        final String sourceNameRecordValue = Optional.ofNullable(sourceNameField).map(record::get).orElse(hostIp);
        final Optional<String> optRegex = Optional.ofNullable(csv.getSourceNameRegex());
        header.setSourceName(optRegex.map(regex -> sourceNameRecordValue.replaceAll(regex, ""))
            .orElse(sourceNameRecordValue));
        header.setVersion(CommonEventHeader.Version._4_0);
        header.setVesEventListenerVersion(Optional.ofNullable(config.getVesEventListenerVersion())
                .map(CommonEventHeader.VesEventListenerVersion::fromValue)
                .orElse(CommonEventHeader.VesEventListenerVersion._7_1));

        final List<String> eventId = csv.getEventId();
        final String keyIdConcat =  eventId.stream()
                .filter(record::containsKey)
                .map(record::get)
                .collect(Collectors.joining());
        header.setEventId(UUID.nameUUIDFromBytes(keyIdConcat.getBytes(StandardCharsets.UTF_8)).toString());
    }
}
