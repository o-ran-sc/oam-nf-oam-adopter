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

package org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client;

import static org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.http.DownloadPerformanceManagementFilesHandler.readPerformanceManagementFiles;
import static org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.http.OffSetTimeZoneHandler.readTimeZone;
import static org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.http.TokenHandler.returnToken;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.reactivex.rxjava3.core.Single;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpHeaders;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.HttpRestClient;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.PerformanceManagementException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.TokenGenerationException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.ZoneIdException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;
import org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.properties.PmEndpointsUrlsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class DefaultHttpRestClient implements HttpRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpRestClient.class);

    public static final String HTTPS = "https://";
    public static final String BEARER = "Bearer ";
    private static final DateTimeFormatter OFFSET_FORMATTER = DateTimeFormatter.ofPattern("xxx");
    private final CloseableHttpAsyncClient client;
    @GuardedBy("this")
    private final LoadingCache<Adapter, String> sessionCache =
            CacheBuilder.newBuilder().refreshAfterWrite(59, TimeUnit.MINUTES).build(new CacheLoader<>() {
                @Override
                public String load(final Adapter adapter) throws ExecutionException, InterruptedException {
                    return returnToken(DefaultHttpRestClient.this.client, DefaultHttpRestClient.this.tokenEndpoint,
                            adapter);
                }
            });

    @GuardedBy("this")
    private final LoadingCache<Adapter, ZoneId> zoneIdCache =
            CacheBuilder.newBuilder().build(new CacheLoader<>() {
                @Override
                public ZoneId load(final Adapter adapter) {
                    return readTimeZone(DefaultHttpRestClient.this, timeZoneEndpoint,  adapter)
                                   .doOnError(error -> LOG.error("Failed to read time zone", error))
                                   .blockingGet();
                }
            });
    private final String pmFilesEndpoint;
    private final String timeZoneEndpoint;
    private final String tokenEndpoint;

    /**
     * Default constructor.
     */
    @Autowired
    public DefaultHttpRestClient(final CloseableHttpAsyncClient httpAsyncClient,
            final PmEndpointsUrlsProperties properties) {
        this.client = httpAsyncClient;
        this.pmFilesEndpoint = properties.getRanPmEndpoint();
        this.timeZoneEndpoint = properties.getRanTimeZoneOffsetEndpoint();
        this.tokenEndpoint = properties.getRanTokenEndpoint();
    }


    @Override
    public synchronized Single<ZipInputStream> readFiles(final Adapter adapter) {
        return readPerformanceManagementFiles(this, pmFilesEndpoint, adapter);
    }

    @Override
    public Single<ZoneId> getTimeZone(final Adapter adapter) {
        try {
            final ZoneId zoneId = zoneIdCache.get(adapter);
            final String offset = OFFSET_FORMATTER.format(zoneId.getRules().getOffset(Instant.now()));
            LOG.info("Adapter {} has offset {}", adapter.getHostIpAddress(), offset);
            return Single.just(zoneId);
        } catch (final Exception e) {
            final Throwable cause = e.getCause();
            if (cause instanceof PerformanceManagementException) {
                return Single.error(cause);
            }
            return Single.error(new ZoneIdException("Failed to get Zone ID for " + adapter.getHostIpAddress(), cause));
        }
    }

    /**
     * Execute GET request on adapter endpoint.
     * @param adapter destiny
     * @param url endpoint
     * @return response
     */
    public Single<SimpleHttpResponse> get(final Adapter adapter, final String url) {
        return getToken(adapter).flatMap(token -> {
            final SimpleHttpRequest request =
                SimpleHttpRequests.get(HTTPS + adapter.getHostIpAddress() + url);
            request.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
            request.addHeader(HttpHeaders.AUTHORIZATION, BEARER + token);
            return Single.fromFuture(client.execute(request, null))
                    .doOnSubscribe(result -> LOG.trace("GET Request started {} ...", request))
                    .doOnSuccess(result -> LOG.trace("GET Request finished {}", request));
        });
    }

    private Single<String> getToken(final Adapter adapter) {
        try {
            final String token = sessionCache.get(adapter);
            return Single.just(token);
        } catch (final Exception e) {
            if (e.getCause() instanceof TokenGenerationException || e.getCause() instanceof ConnectionClosedException) {
                return Single.error(e.getCause());
            }
            return Single.error(e);
        }
    }
}
