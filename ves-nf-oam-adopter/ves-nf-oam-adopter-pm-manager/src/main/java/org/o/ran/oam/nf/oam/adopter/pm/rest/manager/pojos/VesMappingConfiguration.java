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

package org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VesMappingConfiguration {
    @JsonProperty("reporting-entity-name")
    private String reportingEntityName;
    @JsonProperty("reporting-entity-id")
    private String reportingEntityId;
    @JsonProperty("nf-vendor-name")
    private String nfVendorName;
    @JsonProperty("event-source-type")
    private String eventSourceType;
    @JsonProperty("event-name")
    private String eventName;
    @JsonProperty("measurement-interval")
    private Long measurementInterval;
    @JsonProperty("csv")
    private CsvConfiguration csv;
    @JsonProperty("batch-size")
    private int batchSize;
    @JsonProperty("priority")
    private String priority;
    @JsonProperty("ves-event-listener-version")
    private String vesEventListenerVersion;
}
