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

package org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.http;

import com.google.gson.Gson;
import io.reactivex.rxjava3.core.Single;
import java.time.ZoneId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.PerformanceManagementEmptyOutputException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.PerformanceManagementException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;
import org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.DefaultHttpRestClient;
import org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.pojos.TimeZoneResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OffSetTimeZoneHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OffSetTimeZoneHandler.class);

    private static final Gson GSON = new Gson();

    /**
     * Returns time zone of the device.
     */
    public static Single<ZoneId> readTimeZone(final DefaultHttpRestClient httpRestClient, final String offsetEndpoint,
            final Adapter adapter) {
        LOG.debug("Read Time Zone from adapter {}", adapter.getHostIpAddress());
        return httpRestClient.get(adapter, offsetEndpoint)
                       .map(response -> validateGet(response, adapter))
                       .map(ZoneId::of);
    }

    private static String validateGet(final SimpleHttpResponse response, final Adapter adapter) {
        if (response.getCode() != HttpStatus.SC_OK) {
            throw new PerformanceManagementException(
                    "Get Zone offset failed for " + adapter.getHostIpAddress() + " Code: " + response.getCode());
        }
        final String output = response.getBody().getBodyText();
        if (output.isEmpty()) {
            throw new PerformanceManagementEmptyOutputException(
                    "Get Zone offset failed for " + adapter.getHostIpAddress() + " . Empty output received");
        }
        return GSON.fromJson(output, TimeZoneResponse.class).getOffset();
    }
}
