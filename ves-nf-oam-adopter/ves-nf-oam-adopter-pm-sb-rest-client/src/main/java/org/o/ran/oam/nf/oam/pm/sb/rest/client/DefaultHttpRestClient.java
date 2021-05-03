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

package org.o.ran.oam.nf.oam.pm.sb.rest.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipInputStream;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.StatusLine;
import org.checkerframework.checker.lock.qual.GuardedBy;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.http.DownloadPerformanceManagementFiles;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.http.OffSetZoneHttpHandler;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.http.PerformanceManagementFilesHttpHandler;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.http.Urls;
import org.o.ran.oam.nf.oam.pm.sb.rest.client.pojos.TokenResponse;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.HttpRestClient;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.TokenGenerationException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.ZoneIdException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public final class DefaultHttpRestClient implements HttpRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultHttpRestClient.class);

    private static final Gson GSON = new Gson();
    private final CloseableHttpAsyncClient client;
    @GuardedBy("this")
    private final LoadingCache<Adapter, String> sessionCache =
            CacheBuilder.newBuilder().refreshAfterWrite(59, TimeUnit.MINUTES).build(new CacheLoader<>() {
                @Override
                public String load(final Adapter adapter) throws ExecutionException, InterruptedException {
                    try {
                        return returnToken(adapter);
                    } catch (final Exception error) {
                        LOG.error("Failed to read time zone", error);
                        throw error;
                    }
                }
            });

    @GuardedBy("this")
    private final LoadingCache<Adapter, ZoneId> zoneIdCache =
            CacheBuilder.newBuilder().build(new CacheLoader<>() {
                @Override
                public ZoneId load(final Adapter adapter) {
                    return OffSetZoneHttpHandler.readTimeZone(DefaultHttpRestClient.this, adapter)
                                   .doOnError(error -> LOG.error("Failed to read time zone", error))
                                   .blockingGet();
                }
            });

    @Autowired
    public DefaultHttpRestClient(final CloseableHttpAsyncClient httpAsyncClient) {
        this.client = httpAsyncClient;
    }

    private synchronized String returnToken(final Adapter adapter) throws ExecutionException, InterruptedException {
        final String host = Urls.HTTPS.get() + adapter.getHostIpAddress();
        final SimpleHttpRequest request = SimpleHttpRequests.post(host + Urls.TOKEN_URL.get());
        final String basicAuth = Base64.getEncoder()
                .encodeToString((adapter.getUsername() + ":" + adapter.getPassword()).getBytes(StandardCharsets.UTF_8));
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth);
        request.addHeader(HttpHeaders.CONTENT_TYPE, Urls.APPLICATION_JSON.get());
        request.addHeader(HttpHeaders.ACCEPT, Urls.APPLICATION_JSON);

        final SimpleHttpResponse response = this.client.execute(request, null).get();
        final String statusLine = new StatusLine(response).toString();
        if (response.getCode() != HttpStatus.SC_OK) {
            throw new TokenGenerationException("Failed to obtain a token for host " + host + ": " + statusLine);
        }
        final String output = response.getBody().getBodyText();
        if (output.isEmpty()) {
            throw new TokenGenerationException(
                    "Failed to obtain a token for host " + host + ", empty output: " + statusLine);
        }
        final TokenResponse tokenResponse = GSON.fromJson(output, TokenResponse.class);
        return tokenResponse.getToken();
    }

    @Override
    public synchronized Maybe<ZipInputStream> readFiles(final Adapter adapter) {
        return getTimeZone(adapter)
                .flatMap(zoneId -> PerformanceManagementFilesHttpHandler.listPmFiles(this, adapter, zoneId))
                .flatMapMaybe(fileList -> DownloadPerformanceManagementFiles
                        .readPerformanceManagementFiles(this, adapter, fileList));
    }

    @Override
    public Single<ZoneId> getTimeZone(final Adapter adapter) {
        try {
            final ZoneId zoneId = zoneIdCache.get(adapter);
            LOG.info("Adapter {} has timezone {}", adapter.getHostIpAddress(), zoneId.getId());
            return Single.just(zoneId);
        } catch (final ExecutionException e) {
            return Single.error(new ZoneIdException("Failed to get Zone ID for "
                + adapter.getHostIpAddress(), e.getCause()));
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
                SimpleHttpRequests.get(Urls.HTTPS.get() + adapter.getHostIpAddress() + url);
            request.addHeader(HttpHeaders.CONTENT_TYPE, Urls.APPLICATION_JSON);
            request.addHeader(HttpHeaders.AUTHORIZATION, Urls.BEARER.get() + token);
            return Single.fromFuture(client.execute(request, null))
                    .doOnSubscribe(result -> LOG.trace("GET Request started {} ...", request.toString()))
                    .doOnSuccess(result -> LOG.trace("GET Request finished {}", request));
        });
    }

    /**
     * Execute POST request on adapter endpoint.
     * @param adapter destiny
     * @param urlDestiny endpoint
     * @param payload to be sent
     * @return response
     */
    public Single<SimpleHttpResponse> post(final Adapter adapter, final String urlDestiny, final String payload) {
        return getToken(adapter).flatMap(token -> {
            final SimpleHttpRequest request = SimpleHttpRequests
                    .post(Urls.HTTPS.get() + adapter.getHostIpAddress() + urlDestiny);
            request.setBody(payload, ContentType.APPLICATION_JSON);
            request.addHeader(HttpHeaders.AUTHORIZATION, Urls.BEARER.get() + token);
            return Single.fromFuture(client.execute(request, null))
                    .doOnSubscribe(result -> LOG.trace("POST Request started {} ...", request.toString()))
                    .doOnSuccess(result -> LOG.trace("POST Request finished {}", request));
        });
    }

    private Single<String> getToken(final Adapter adapter) {
        try {
            final String token = sessionCache.get(adapter);
            return Single.just(token);
        } catch (final Exception e) {
            if (e.getCause() instanceof TokenGenerationException) {
                return Single.error(e.getCause());
            } else if (e.getCause() instanceof ConnectionClosedException) {
                return Single.error(e.getCause());
            }
            return Single.error(e);
        }
    }
}
