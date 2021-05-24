package org.o.ran.oam.nf.oam.adopter.spi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MapperConfigProvider<T> {

    private static final Logger LOG = LoggerFactory.getLogger(MapperConfigProvider.class);

    private static final ObjectMapper YAML_READER = new ObjectMapper(new YAMLFactory());
    private ReloadingFileBasedConfigurationBuilder<YAMLConfiguration> builder;

    /**
     * Initialize Service.
     */
    @PostConstruct
    public final void init() throws IOException, ConfigurationException {
        final var filePath = Paths.get(getMappingFilePath()).toUri();
        builder = new ReloadingFileBasedConfigurationBuilder<>(YAMLConfiguration.class)
                          .configure(new Parameters().hierarchical().setURL(filePath.toURL()));
        builder.addEventListener(ConfigurationBuilderEvent.CONFIGURATION_REQUEST, event -> {
            builder.getReloadingController().checkForReloading(null);
            LOG.debug("Reloading {}", filePath);
        });
        //Test initial configuration
        builder.getConfiguration();
    }

    public abstract String getMappingFilePath();

    /**
     * Provide VES Mapping configuration.
     */
    public final T getVesMappingConfiguration() throws ConfigurationException, IOException {
        final YAMLConfiguration configuration = builder.getConfiguration();
        final var output = new StringWriter();
        configuration.write(output);
        return YAML_READER.readValue(output.toString(), getClazz());
    }

    public abstract Class<T> getClazz();
}
