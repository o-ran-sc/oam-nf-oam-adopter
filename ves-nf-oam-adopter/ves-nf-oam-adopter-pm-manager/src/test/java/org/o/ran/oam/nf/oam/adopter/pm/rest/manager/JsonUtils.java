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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

@UtilityClass
final class JsonUtils {
    private static final List<String> WHITE_LIST = Arrays.asList("eventId", "startEpochMicrosec", "lastEpochMicrosec");
    private static final String EVENT_LIST = "eventList";
    private static final String COMMON_EVENT_HEADER = "commonEventHeader";

    static String readJson(final String url) throws IOException {
        return IOUtils.toString(JsonUtils.class.getResourceAsStream(url), StandardCharsets.UTF_8);
    }

    public static void compareResult(final String expected, final String actual) {
        final JsonObject expectedJO = JsonParser.parseString(expected).getAsJsonObject();
        final JsonObject actualJO = JsonParser.parseString(actual).getAsJsonObject();
        removeCommonEventHeaderFields(expectedJO.get(EVENT_LIST).getAsJsonArray(),
                actualJO.get(EVENT_LIST).getAsJsonArray(), WHITE_LIST);
        assertEquals(expectedJO, actualJO);
    }

    private static void removeCommonEventHeaderFields(final JsonArray expectedJO, final JsonArray actualJO,
            final List<String> asList) {
        asList.forEach(wipe -> {
            expectedJO.forEach(jsonElement -> removeCommonEventHeaderFields(jsonElement, wipe));
            actualJO.forEach(jsonElement -> removeCommonEventHeaderFields(jsonElement, wipe));
        });
    }

    private static void removeCommonEventHeaderFields(final JsonElement jsonElement, final String wipe) {
        jsonElement.getAsJsonObject().getAsJsonObject(COMMON_EVENT_HEADER).remove(wipe);
    }
}
