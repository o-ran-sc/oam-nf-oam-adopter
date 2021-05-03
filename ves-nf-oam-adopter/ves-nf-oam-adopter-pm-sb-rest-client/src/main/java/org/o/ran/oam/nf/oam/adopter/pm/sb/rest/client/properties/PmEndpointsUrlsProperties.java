package org.o.ran.oam.nf.oam.adopter.pm.sb.rest.client.properties;

import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "pm-rest-manager")
@Data
@NoArgsConstructor
@Validated
public class PmEndpointsUrlsProperties {

    @NotEmpty
    private String ranTokenEndpoint;
    @NotEmpty
    private String ranPmEndpoint;
    @NotEmpty
    private String ranTimeZoneOffsetEndpoint;
}
