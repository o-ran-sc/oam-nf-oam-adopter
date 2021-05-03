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

package org.o.ran.oam.nf.oam.adopter.snmp.manager.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TrapsMappingConfiguration {
    @JsonProperty("oid")
    private String oid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("oid-event-id")
    private String oidEventId;
    @JsonProperty("event-severity")
    private String oidEventSeverity;
    @JsonProperty("oid-specific-problem-desc")
    private String oidSpecificProblemDesc;
    @JsonProperty("oid-source-name")
    private String oidSourceName;
    @JsonProperty("oid-reporting-entity-id")
    private String oidReportingEntityID;
    @JsonProperty("oid-alarm-interface-name")
    private String oidAlarmInterfaceName;
    @JsonProperty("event-category")
    private String eventCategory;
    @JsonProperty("event-source-type")
    private String eventSourceType;
    @JsonProperty("oid-event-sequence")
    private String oidEventSequence;
    @JsonProperty("oid-start-epoch-microsec")
    private String eventStartEpochMicrosec;
    @JsonProperty("oid-last-epoch-microsec")
    private String eventLastEpochMicrosec;
}
