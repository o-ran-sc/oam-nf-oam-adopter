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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.rxjava3.core.Single;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.o.ran.oam.nf.oam.adopter.api.CommonEventFormat302ONAP;
import org.o.ran.oam.nf.oam.adopter.api.Event;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.PerformanceManagementMapperConfigProvider;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.CsvConfiguration;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.VesMappingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PerformanceManagementFile2VesMapper {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceManagementFile2VesMapper.class);

    private static final String CSV_EXTENSION = ".csv";
    private final PerformanceManagementMapperConfigProvider pmConfigProvider;

    @Autowired
    public PerformanceManagementFile2VesMapper(final PerformanceManagementMapperConfigProvider pmConfigProvider) {
        this.pmConfigProvider = pmConfigProvider;
    }

    /**
     * Translate CSV in ZipInputStream format to list of CommonEventFormat302ONAP events.
     *
     * @param zipInputStream csv
     * @param hostIp source Ip Address
     * @return CommonEventFormat302ONAP events
     */
    @SuppressFBWarnings("REC_CATCH_EXCEPTION")
    public Single<List<CommonEventFormat302ONAP>> map(final ZipInputStream zipInputStream, final String hostIp) {
        LOG.info("Converting ZIP files to VES Message started");
        final List<CommonEventFormat302ONAP> listOfNotifications = new ArrayList<>();
        final CsvSchema schema = CsvSchema.emptySchema().withHeader();
        final CsvMapper mapper = new CsvMapper();
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        try {
            ZipEntry entry;
            final VesMappingConfiguration mappingConfiguration = pmConfigProvider.getVesMappingConfiguration();
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final String entryName = entry.getName();
                if (!entryName.endsWith(CSV_EXTENSION)) {
                    return Single.error(new Exception("Wrong file type :" + entryName));
                }

                final Iterator<Map<String, String>> iterator =
                        mapper.readerFor(Map.class).with(schema).readValues(zipInputStream);
                final List<List<Event>> mappedEvents = toEvent(mappingConfiguration, hostIp, iterator);

                mappedEvents.forEach(mapped -> {
                    final CommonEventFormat302ONAP eventFormat = new CommonEventFormat302ONAP();
                    eventFormat.setEventList(mapped);
                    listOfNotifications.add(eventFormat);
                });
            }
        } catch (final Exception e) {
            return Single.error(new Exception("Failed to process file", e));
        } finally {
            try {
                zipInputStream.closeEntry();
            } catch (final IOException e) {
                LOG.warn("Failed to close zip stream", e);
            }
        }
        LOG.info("Converting ZIP files to VES Message finished");
        return Single.just(listOfNotifications);
    }

    private static List<List<Event>> toEvent(final VesMappingConfiguration mappingConfiguration, final String hostIp,
            final Iterator<Map<String, String>> iterator) {
        final List<List<Event>> globalList = new ArrayList<>();
        final int batchSize = mappingConfiguration.getBatchSize();
        int sequence = 0;
        List<Event> events = new ArrayList<>();
        final CsvConfiguration csv = mappingConfiguration.getCsv();
        while (iterator.hasNext()) {
            final Event event = new Event();
            final Map<String, String> recordMap = iterator.next();
            event.setCommonEventHeader(CommonEventHeaderHandler.toCommonEventHeader(mappingConfiguration, hostIp, csv,
                recordMap,  sequence));
            event.setMeasurementFields(MeasurementFieldsHandler.toMeasurementFields(mappingConfiguration, recordMap));
            events.add(event);
            sequence++;
            if (sequence % batchSize == 0) {
                globalList.add(events);
                events = new ArrayList<>();
            }
        }
        if (!events.isEmpty()) {
            globalList.add(events);
        }
        return globalList;
    }
}
