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

package org.o.ran.oam.nf.oam.adopter.snmp.manager;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;
import org.apache.commons.configuration2.YAMLConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.pojos.VesMappingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Reads and listens for changes on snmp mapping configuration.
 */
@Service
public class SnmpMappingConfigurationProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpMappingConfigurationProvider.class);

    private static final ObjectMapper YAML_READER = new ObjectMapper(new YAMLFactory());
    @Value("${snmp-manager.mapping-config-path:#{null}}")
    private String mappingFilePath;
    private ReloadingFileBasedConfigurationBuilder<YAMLConfiguration> builder;

    /**
     * Initialize service.
     * @throws IOException on error
     */
    @PostConstruct
    public void init() throws IOException, ConfigurationException {
        requireNonNull(mappingFilePath);
        final URI filePath = Paths.get(mappingFilePath).toUri();
        builder = new ReloadingFileBasedConfigurationBuilder<>(YAMLConfiguration.class)
                .configure(new Parameters().hierarchical().setURL(filePath.toURL()));
        builder.addEventListener(ConfigurationBuilderEvent.CONFIGURATION_REQUEST, event -> {
            builder.getReloadingController().checkForReloading(null);
            LOG.info("Reloading {}", filePath);
        });
        //Test initial configuration
        builder.getConfiguration();
    }

    /**
     * Reads VesMappingConfiguration from yaml file.
     *
     * @return Ves Mapping Configuration
     */
    public VesMappingConfiguration getVesMappingConfiguration() throws ConfigurationException, IOException {
        final YAMLConfiguration configuration = builder.getConfiguration();
        final StringWriter output = new StringWriter();
        configuration.write(output);
        return YAML_READER.readValue(output.toString(), VesMappingConfiguration.class);
    }
}
