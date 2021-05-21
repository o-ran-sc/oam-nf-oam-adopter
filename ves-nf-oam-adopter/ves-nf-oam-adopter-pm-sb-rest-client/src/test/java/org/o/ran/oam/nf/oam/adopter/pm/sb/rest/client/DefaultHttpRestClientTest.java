package org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import io.reactivex.rxjava3.observers.TestObserver;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.zip.ZipInputStream;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.stubbing.Answer;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.HttpRestClient;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;
import org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.properties.PmEndpointsUrlsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;

@SpringBootTest(classes = {DefaultHttpRestClient.class, PmEndpointsUrlsProperties.class})
@EnableConfigurationProperties
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DefaultHttpRestClientTest {

    private static final Adapter ADAPTER =
            Adapter.builder().hostIpAddress("150.62.25.26").username("admin").password("secretPassword").build();
    @Autowired
    public HttpRestClient restClient;

    @MockBean
    CloseableHttpAsyncClient client;

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    void testGetFailedToken() {
        final SimpleHttpResponse response =
                SimpleHttpResponse.create(HttpStatus.SC_UNAUTHORIZED, "error", ContentType.APPLICATION_JSON);

        when(client.execute(any(SimpleHttpRequest.class), nullable(FutureCallback.class)))
                .thenAnswer((Answer<Future<SimpleHttpResponse>>) invocation -> {
                    final SimpleHttpRequest req = (SimpleHttpRequest) invocation.getArguments()[0];
                    if ("/auth/token".equals(req.getPath())) {
                        return CompletableFuture.completedFuture(response);
                    }
                    throw new IllegalStateException("Unexpected value: " + req.getPath());
                });


        final TestObserver<ZoneId> observer = restClient.getTimeZone(ADAPTER).test();
        observer.assertError(throwable -> throwable.getMessage()
            .equals("Failed to obtain a token for host https://150.62.25.26: HTTP/1.1 401 Unauthorized"));
    }

    @Test
    void testReadFiles() throws IOException {
        final String tokenJson = JsonUtils.readJson("/json/tokenResponse.json");
        final SimpleHttpResponse response =
                SimpleHttpResponse.create(HttpStatus.SC_OK, tokenJson, ContentType.APPLICATION_JSON);

        final SimpleHttpResponse zipResponse =
                SimpleHttpResponse.create(HttpStatus.SC_OK, "mockZip", ContentType.APPLICATION_OCTET_STREAM);

        when(client.execute(any(SimpleHttpRequest.class), nullable(FutureCallback.class)))
                .thenAnswer((Answer<Future<SimpleHttpResponse>>) invocation -> {
                    final SimpleHttpRequest req = (SimpleHttpRequest) invocation.getArguments()[0];
                    switch (req.getPath()) {
                        case "/auth/token":
                            return CompletableFuture.completedFuture(response);
                        case "/pm/files":
                            return CompletableFuture.completedFuture(zipResponse);
                        default:
                            throw new IllegalStateException("Unexpected value: " + req.getPath());
                    }
                });


        final TestObserver<ZipInputStream> observer = restClient.readFiles(ADAPTER).test();
        final ZipInputStream expected = new ZipInputStream(new ByteArrayInputStream("mockZip".getBytes()));
        observer.assertValue(zip -> Arrays.equals(zip.readAllBytes(), expected.readAllBytes()));
    }

    @Test
    void testReadFilesResponseFail() throws IOException {
        final String tokenJson = JsonUtils.readJson("/json/tokenResponse.json");
        final SimpleHttpResponse response =
                SimpleHttpResponse.create(HttpStatus.SC_OK, tokenJson, ContentType.APPLICATION_JSON);

        final SimpleHttpResponse zipResponse = SimpleHttpResponse.create(HttpStatus.SC_BAD_REQUEST, "mockZip",
                ContentType.APPLICATION_OCTET_STREAM);

        when(client.execute(any(SimpleHttpRequest.class), nullable(FutureCallback.class)))
                .thenAnswer((Answer<Future<SimpleHttpResponse>>) invocation -> {
                    final SimpleHttpRequest req = (SimpleHttpRequest) invocation.getArguments()[0];
                    switch (req.getPath()) {
                        case "/auth/token":
                            return CompletableFuture.completedFuture(response);
                        case "/pm/files":
                            return CompletableFuture.completedFuture(zipResponse);
                        default:
                            throw new IllegalStateException("Unexpected value: " + req.getPath());
                    }
                });


        final TestObserver<ZipInputStream> observer = restClient.readFiles(ADAPTER).test();
        observer.assertError(throwable -> throwable.getMessage()
            .equals("Download files from 150.62.25.26 failed: HTTP/1.1 400 Bad Request"));
    }

    @Test
    void testGetTimeOffset() throws IOException {
        final String tokenJson = JsonUtils.readJson("/json/tokenResponse.json");
        final SimpleHttpResponse response =
                SimpleHttpResponse.create(HttpStatus.SC_OK, tokenJson, ContentType.APPLICATION_JSON);

        final String timeZoneOFfsetResponseJson = JsonUtils.readJson("/json/timeZoneOffsetResponse.json");
        final SimpleHttpResponse timeOffsetResponse =
                SimpleHttpResponse.create(HttpStatus.SC_OK, timeZoneOFfsetResponseJson, ContentType.APPLICATION_JSON);

        when(client.execute(any(SimpleHttpRequest.class), nullable(FutureCallback.class)))
                .thenAnswer((Answer<Future<SimpleHttpResponse>>) invocation -> {
                    final SimpleHttpRequest req = (SimpleHttpRequest) invocation.getArguments()[0];
                    switch (req.getPath()) {
                        case "/auth/token":
                            return CompletableFuture.completedFuture(response);
                        case "/system/timeZone":
                            return CompletableFuture.completedFuture(timeOffsetResponse);
                        default:
                            throw new IllegalStateException("Unexpected value: " + req.getPath());
                    }
                });


        final TestObserver<ZoneId> observer = restClient.getTimeZone(ADAPTER).test();
        observer.assertValues(ZoneId.of("+02:00"));
    }

    @Test
    void testGetTimeOffsetFail() throws IOException {
        final String tokenJson = JsonUtils.readJson("/json/tokenResponse.json");
        final SimpleHttpResponse response =
                SimpleHttpResponse.create(HttpStatus.SC_OK, tokenJson, ContentType.APPLICATION_JSON);

        final SimpleHttpResponse timeOffsetResponse =
                SimpleHttpResponse.create(HttpStatus.SC_OK, "", ContentType.APPLICATION_JSON);

        when(client.execute(any(SimpleHttpRequest.class), nullable(FutureCallback.class)))
                .thenAnswer((Answer<Future<SimpleHttpResponse>>) invocation -> {
                    final SimpleHttpRequest req = (SimpleHttpRequest) invocation.getArguments()[0];
                    switch (req.getPath()) {
                        case "/auth/token":
                            return CompletableFuture.completedFuture(response);
                        case "/system/timeZone":
                            return CompletableFuture.completedFuture(timeOffsetResponse);
                        default:
                            throw new IllegalStateException("Unexpected value: " + req.getPath());
                    }
                });


        final TestObserver<ZoneId> observer = restClient.getTimeZone(ADAPTER).test();
        observer.assertError(throwable -> throwable.getMessage()
            .equals("Get Zone offset failed for 150.62.25.26 . Empty output received"));
    }
}