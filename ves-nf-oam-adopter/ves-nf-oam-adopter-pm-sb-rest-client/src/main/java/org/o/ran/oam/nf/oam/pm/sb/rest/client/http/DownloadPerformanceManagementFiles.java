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
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.zip.ZipInputStream;
import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.StatusLine;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.DefaultHttpRestClient;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.pojos.FileListRequest;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.PerformanceManagementException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DownloadPerformanceManagementFiles {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadPerformanceManagementFiles.class);

    private static final Gson GSON = new Gson();
    private static final String ZIP_MIME_TYPE = "application/zip";

    /**
     * Reads and return ZIP CSV PM Files from device.
     */
    public static Maybe<ZipInputStream> readPerformanceManagementFiles(final DefaultHttpRestClient httpSession,
            final Adapter adapter, final List<String> fileList) {
        if (fileList.isEmpty()) {
            return Maybe.empty();
        }
        LOG.info("Download PM files from adapter {}", adapter.getHostIpAddress());
        final String payload = GSON.toJson(new FileListRequest(fileList));
        return httpSession.post(adapter, Urls.DOWNLOAD_PIM_CANCELER_FILES_URL.get(), payload)
                .flatMap(response -> DownloadPerformanceManagementFiles.validateGetZipFile(adapter, response))
                .map(entity -> new ZipInputStream(new ByteArrayInputStream(entity.getBodyBytes()))).toMaybe();
    }

    private static Single<SimpleBody> validateGetZipFile(final Adapter adapter, final SimpleHttpResponse response) {
        final String statusLine = new StatusLine(response).toString();
        final ContentType contentType = response.getContentType();
        final SimpleBody entity = response.getBody();
        if (response.getCode() == HttpStatus.SC_OK && entity != null) {
            if (contentType != null && contentType.getMimeType().equals(ZIP_MIME_TYPE)) {
                return Single.just(entity);
            }
        }
        return Single.error(new PerformanceManagementException(
                "Download files from " + adapter.getHostIpAddress() + " failed: " + statusLine + " " + entity));
    }
}
