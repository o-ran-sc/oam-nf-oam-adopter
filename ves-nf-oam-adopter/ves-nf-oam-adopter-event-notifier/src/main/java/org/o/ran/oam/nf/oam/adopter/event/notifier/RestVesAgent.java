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

package org.o.ran.oam.nf.oam.adopter.event.notifier;

import static java.util.Objects.requireNonNull;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.StatusLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RestVesAgent {

    private static final Logger LOG = LoggerFactory.getLogger(RestVesAgent.class);
    private static final String EVENT_BATCH = "/eventBatch";

    private final CloseableHttpAsyncClient httpClient;
    private final String vesUrl;
    private final String authHeader;

    /**
     * Default constructor.
     */
    public RestVesAgent(final String vesUrl, final String vesEncodedAuth, final CloseableHttpAsyncClient httpClient) {
        LOG.debug("VES Collector {}", vesUrl);
        this.vesUrl = requireNonNull(vesUrl, "VES Url must not be null");
        this.authHeader = "Basic " + requireNonNull(vesEncodedAuth, "VES Authentication must not be null");
        this.httpClient = httpClient;
    }

    public Completable notifyEvent(final String event) {
        return notify(vesUrl, event);
    }

    public Completable notifyEventBatch(final String eventBatch) {
        return notify(vesUrl + EVENT_BATCH, eventBatch);
    }

    private Completable notify(final String url, final String payload) {
        final SimpleHttpRequest request = SimpleHttpRequests.post(url);
        request.setBody(payload, ContentType.APPLICATION_JSON);
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        return Single.fromFuture(httpClient.execute(request, null))
                       .flatMapCompletable(RestVesAgent::validatePost)
                       .doOnSubscribe(result -> LOG.trace("Request sent {} / payload {}", request, payload))
                       .doOnComplete(() -> LOG.debug("Request finished {}", request))
                       .doOnError(error -> LOG.warn("Request failed {}", request, error));
    }

    private static Completable validatePost(final SimpleHttpResponse response) {
        final String statusLine = new StatusLine(response).toString();
        final String entity;
        final int code = response.getCode();
        if (code == HttpStatus.SC_OK || code == HttpStatus.SC_ACCEPTED) {
            return Completable.complete();
        }
        entity = response.getBody().getBodyText();
        return Completable.error(new Exception("Failed to post: " + statusLine + " " + entity));
    }
}
