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
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CsvConfiguration {
    @JsonProperty("additional-fields")
    private List<String> additionalFields;
    @JsonProperty("additional-measurements-name")
    private String additionalMeasurementsName;
    @JsonProperty("additional-measurements")
    private List<String> additionalMeasurements;
    @JsonProperty("event-id")
    private List<String> eventId;
    @JsonProperty("source-name")
    private String sourceName;
    @JsonProperty("source-name-regex")
    private String sourceNameRegex;
}
