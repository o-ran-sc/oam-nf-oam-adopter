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

package org.o.ran.oam.nf.oam.adopter.pm.rest.manager;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.zip.ZipInputStream;
import org.eclipse.jdt.annotation.NonNull;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.HttpRestClient;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.Adapter;

public class HttpRestClientMock implements HttpRestClient {

    @Override
    public Maybe<ZipInputStream> readFiles(@NonNull final Adapter adapter) {
        final InputStream file = HttpRestClientMock.class.getResourceAsStream("/zip/nfOamAdapter1.zip");
        if (file == null) {
            return Maybe.error(new Exception("Failed to read test file"));
        }
        final BufferedInputStream bis = new BufferedInputStream(file);
        return Maybe.just(new ZipInputStream(bis));
    }

    @Override
    public @NonNull Single<ZoneId> getTimeZone(@NonNull final Adapter adapter) {
        return Single.just(ZoneId.of("+02:00"));
    }
}
