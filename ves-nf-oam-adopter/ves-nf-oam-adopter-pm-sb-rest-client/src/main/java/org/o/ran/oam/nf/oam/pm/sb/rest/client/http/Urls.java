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

public enum Urls {
    TOKEN_URL("/isco/api/auth/token"),
    LIST_PIM_CANCELER_FILES_URL("/isco/api/system/files/list/pimcanceler"),
    DOWNLOAD_PIM_CANCELER_FILES_URL("/isco/api/system/files/download/pimcanceler"),
    OFFSET_URL("/isco/api/network/config"),
    HTTPS("https://"),
    BEARER("Bearer "),
    APPLICATION_JSON("application/json");

    private final String url;

    Urls(final String url) {
        this.url = url;
    }

    public String get() {
        return url;
    }
}
