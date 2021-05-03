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

package org.o.ran.oam.nf.oam.pm.sb.rest.client.http;

import com.google.gson.Gson;
import io.reactivex.rxjava3.core.Single;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.DefaultHttpRestClient;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.pojos.File;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.pojos.FileListResponse;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.pojos.ListPmFilesRequest;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.PerformanceManagementEmptyOutputException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.PerformanceManagementException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PerformanceManagementFilesHttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceManagementFilesHttpHandler.class);

    private static final Gson GSON = new Gson();
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Collect list of PM files.
     */
    public static Single<List<String>> listPmFiles(final DefaultHttpRestClient httpSession, final Adapter adapter,
            final ZoneId zoneId) {
        final ZonedDateTime now = ZonedDateTime.now(zoneId).minusDays(1);

        final String startDate =
                now.withHour(0).withMinute(0).withSecond(0).format(DATE_TIME_FORMAT);
        final String endDate =
                now.withHour(23).withMinute(59).withSecond(59).format(DATE_TIME_FORMAT);
        final String payload = GSON.toJson(new ListPmFilesRequest(endDate, startDate));
        LOG.info("List PM files from adapter {} from date {} to date {}",
                adapter.getHostIpAddress(), startDate, endDate);

        return httpSession.post(adapter, Urls.LIST_PIM_CANCELER_FILES_URL.get(), payload)
                .map(response -> validateGetPmList(response, adapter));
    }

    private static List<String> validateGetPmList(final SimpleHttpResponse response, final Adapter adapter) {
        if (response.getCode() != HttpStatus.SC_OK) {
            throw new PerformanceManagementException(
                    "Get list of PM files failed for " + adapter.getHostIpAddress() + " Code: " + response.getCode());
        }
        final String output = response.getBody().getBodyText();
        if (output.isEmpty()) {
            throw new PerformanceManagementEmptyOutputException(
                    "Get list of PM files failed for " + adapter.getHostIpAddress() + " . Empty output received");
        }
        final FileListResponse fileList = GSON.fromJson(output, FileListResponse.class);
        return fileList.getFileList().stream().map(File::getName).collect(Collectors.toList());
    }
}
