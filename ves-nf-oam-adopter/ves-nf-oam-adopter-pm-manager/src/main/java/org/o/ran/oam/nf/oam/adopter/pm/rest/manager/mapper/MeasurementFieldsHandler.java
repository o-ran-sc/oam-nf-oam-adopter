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

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.o.ran.oam.nf.oam.adopter.api.MeasurementFields;
import org.o.ran.oam.nf.oam.adopter.api.NamedHashMap;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.CsvConfiguration;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.VesMappingConfiguration;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MeasurementFieldsHandler {
    private static void setOptionalsFields(final MeasurementFields measurementFields, final Map<String, String> records,
            final VesMappingConfiguration config) {
        final CsvConfiguration csv = config.getCsv();
        measurementFields.setAdditionalFields(csv.getAdditionalFields()
                .stream().filter(records::containsKey)
                .collect(Collectors.toMap(Function.identity(), records::get)));

        final NamedHashMap namedHashMap = new NamedHashMap();
        namedHashMap.setName(csv.getAdditionalMeasurementsName());
        namedHashMap.setHashMap(csv.getAdditionalMeasurements().stream()
                .filter(records::containsKey)
                .collect(Collectors.toMap(Function.identity(), records::get)));
        measurementFields.setAdditionalMeasurements(Collections.singletonList(namedHashMap));
        measurementFields.setAdditionalObjects(null);
        measurementFields.setCodecUsageArray(null);
        measurementFields.setConfiguredEntities(null);
        measurementFields.setCpuUsageArray(null);
        measurementFields.setDiskUsageArray(null);
        measurementFields.setFeatureUsageArray(null);
        measurementFields.setFilesystemUsageArray(null);
        measurementFields.setHugePagesArray(null);
        measurementFields.setIpmi(null);
        measurementFields.setLatencyDistribution(null);
        measurementFields.setLoadArray(null);
        measurementFields.setMachineCheckExceptionArray(null);
        measurementFields.setMeanRequestLatency(null);
        measurementFields.setMemoryUsageArray(null);
        measurementFields.setNfcScalingMetric(null);
        measurementFields.setNicPerformanceArray(null);
        measurementFields.setNumberOfMediaPortsInUse(null);
        measurementFields.setProcessStatsArray(null);
        measurementFields.setRequestRate(null);
    }

    static MeasurementFields toMeasurementFields(final VesMappingConfiguration config,
            final Map<String, String> records) {
        final MeasurementFields measurementFields = new MeasurementFields();
        setMandatoryFields(measurementFields, config);
        setOptionalsFields(measurementFields, records, config);
        return measurementFields;
    }

    private static void setMandatoryFields(final MeasurementFields measurementFields,
            final VesMappingConfiguration config) {
        measurementFields.setMeasurementFieldsVersion(MeasurementFields.MeasurementFieldsVersion._4_0);
        measurementFields.setMeasurementInterval(config.getMeasurementInterval());
    }
}
