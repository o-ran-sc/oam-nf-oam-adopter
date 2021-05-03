package org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.http;

import static org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.EndpointUtil.HTTPS;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.StatusLine;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.TokenGenerationException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;
import org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.pojos.TokenResponse;

public final class TokenHandler {

    public static final Gson GSON = new Gson();

    private TokenHandler() {
        throw new UnsupportedOperationException();
    }

    /**
     * Request Token under defined endpoint.
     */
    public static synchronized String returnToken(final CloseableHttpAsyncClient client, final String tokenEndpoint,
            final Adapter adapter) throws ExecutionException, InterruptedException {
        final String host = HTTPS + adapter.getHostIpAddress();
        final SimpleHttpRequest request = SimpleHttpRequests.post(host + tokenEndpoint);
        final String basicAuth = Base64.getEncoder().encodeToString(
                (adapter.getUsername() + ":" + adapter.getPassword()).getBytes(StandardCharsets.UTF_8));
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth);
        request.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        request.addHeader(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE);

        final SimpleHttpResponse response = client.execute(request, null).get();
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
}