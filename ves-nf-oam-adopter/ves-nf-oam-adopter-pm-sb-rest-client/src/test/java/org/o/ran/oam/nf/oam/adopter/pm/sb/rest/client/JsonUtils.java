package org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

@UtilityClass
final class JsonUtils {
    static String readJson(final String url) throws IOException {
        return IOUtils.toString(JsonUtils.class.getResourceAsStream(url), StandardCharsets.UTF_8);
    }
}
