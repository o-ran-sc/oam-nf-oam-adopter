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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.reactivex.rxjava3.observers.TestObserver;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.o.ran.oam.nf.oam.adopter.api.CommonEventFormat302ONAP;
import org.o.ran.oam.nf.oam.adopter.api.Event;
import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.o.ran.oam.nf.oam.adopter.event.notifier.properties.VesCollectorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {NotificationProvider.class, VesCollectorProperties.class})
@EnableConfigurationProperties
public class NotificationProviderTest {

    @Autowired
    VesEventNotifier vesEventNotifier;

    @MockBean
    CloseableHttpAsyncClientMock client;

    @Test
    void testNotifySingleEvent() {
        final SimpleHttpResponse response = new SimpleHttpResponse(HttpStatus.SC_OK);
        final Future<SimpleHttpResponse> completableFuture = CompletableFuture.completedFuture(response);
        when(client.doExecute(any(), any(), any(), any(), any(), any()))
                .thenAnswer((Answer<Future<SimpleHttpResponse>>) invocation -> completableFuture);

        final CommonEventFormat302ONAP commonEvent = new CommonEventFormat302ONAP();
        commonEvent.setEvent(new Event());
        final TestObserver<Void> observer = vesEventNotifier.notifyEvents(commonEvent).test();
        observer.assertComplete();
    }

    @Test
    void testNotifySingleEventFail() {
        final SimpleHttpResponse response = new SimpleHttpResponse(HttpStatus.SC_BAD_REQUEST);
        response.setBody("some error", ContentType.APPLICATION_JSON);
        final Future<SimpleHttpResponse> completableFuture = CompletableFuture.completedFuture(response);
        when(client.doExecute(any(), any(), any(), any(), any(), any()))
                .thenAnswer((Answer<Future<SimpleHttpResponse>>) invocation -> completableFuture);
        final CommonEventFormat302ONAP commonEvent = new CommonEventFormat302ONAP();
        commonEvent.setEvent(new Event());
        final TestObserver<Void> observer = vesEventNotifier.notifyEvents(commonEvent).test();
        observer.assertError(throwable -> throwable.getMessage()
                                                  .equals("Failed to post: HTTP/1.1 400 Bad Request some error"));
    }

    @Test
    void testNotifyEventBatch() {
        final SimpleHttpResponse response = new SimpleHttpResponse(HttpStatus.SC_OK);
        final Future<SimpleHttpResponse> completableFuture = CompletableFuture.completedFuture(response);
        when(client.doExecute(any(), any(), any(), any(), any(), any()))
                .thenAnswer((Answer<Future<SimpleHttpResponse>>) invocation -> completableFuture);

        final CommonEventFormat302ONAP commonEvent = new CommonEventFormat302ONAP();
        commonEvent.setEventList(Collections.singletonList(new Event()));
        final TestObserver<Void> observer = vesEventNotifier.notifyEvents(commonEvent).test();
        observer.assertComplete();
    }

    private abstract static class CloseableHttpAsyncClientMock extends CloseableHttpAsyncClient {

        protected abstract <T> Future<T> doExecute(HttpHost var1, AsyncRequestProducer var2,
                AsyncResponseConsumer<T> var3, HandlerFactory<AsyncPushConsumer> var4, HttpContext var5,
                FutureCallback<T> var6);
    }
}