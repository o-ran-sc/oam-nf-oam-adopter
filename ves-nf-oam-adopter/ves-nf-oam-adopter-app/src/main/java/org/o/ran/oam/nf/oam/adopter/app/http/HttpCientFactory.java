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

package org.o.ran.oam.nf.oam.adopter.app.http;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http2.HttpVersionPolicy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpCientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(HttpCientFactory.class);

    /**
     * Generates a CloseableHttpAsyncClient.
     */
    public static CloseableHttpAsyncClient createClient(final String trustStore,
            final String trustStorePassword, final Long conectionTimeout, final Long responseTimeout)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException,
            CertificateException {
        final var sslContext = getSslContext(new File(trustStore), trustStorePassword);
        return trustTrustStore(sslContext, conectionTimeout, responseTimeout);
    }

    private static SSLContext getSslContext(final File trustStoreFilePath, final String trustStorePassword)
            throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException {
        return new SSLContextBuilder()
                .loadTrustMaterial(trustStoreFilePath.toURI().toURL(), trustStorePassword.toCharArray())
                .build();
    }

    private static CloseableHttpAsyncClient trustTrustStore(final SSLContext sslContext,
            final Long conectionTimeout, final Long responseTimeout) {
        LOG.info("Trust all certificates under truststore");
        final PoolingAsyncClientConnectionManager connectionManager =
                PoolingAsyncClientConnectionManagerBuilder.create().setTlsStrategy(
                        ClientTlsStrategyBuilder.create()
                                .setSslContext(sslContext)
                                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .build())
                        .build();

        return HttpAsyncClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(createDefaultRequestConfig(conectionTimeout, responseTimeout))
                .setVersionPolicy(HttpVersionPolicy.NEGOTIATE)
                .build();
    }

    private static RequestConfig createDefaultRequestConfig(final Long conectionTimeout, final Long responseTimeout) {
        return RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(conectionTimeout))
                .setResponseTimeout(Timeout.ofSeconds(responseTimeout))
                .setCookieSpec(StandardCookieSpec.STRICT)
                .build();
    }
}
