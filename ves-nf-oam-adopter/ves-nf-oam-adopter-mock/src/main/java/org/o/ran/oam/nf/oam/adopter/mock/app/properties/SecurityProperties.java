package org.o.ran.oam.nf.oam.adopter.mock.app.properties;

import javax.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.auth")
@Data
@NoArgsConstructor
public class SecurityProperties {
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
}
