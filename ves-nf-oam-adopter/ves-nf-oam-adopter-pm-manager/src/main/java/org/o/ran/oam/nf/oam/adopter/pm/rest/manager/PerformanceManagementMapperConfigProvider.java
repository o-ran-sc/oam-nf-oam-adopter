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
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.pojos.VesMappingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PerformanceManagementMapperConfigProvider  {

    private static final Logger LOG = LoggerFactory.getLogger(PerformanceManagementMapperConfigProvider.class);

    private static final ObjectMapper YAML_READER = new ObjectMapper(new YAMLFactory());
    @Value("${pm-rest-manager.mapping-config-path:#{null}}")
    private String mappingFilePath;
    private ReloadingFileBasedConfigurationBuilder<YAMLConfiguration> builder;

    @Autowired
    public PerformanceManagementMapperConfigProvider() {

    }

    /**
     * Initialize Service.
     */
    @PostConstruct
    public void init() throws IOException, ConfigurationException {
        requireNonNull(mappingFilePath);
        final URI filePath = Paths.get(mappingFilePath).toUri();
        builder = new ReloadingFileBasedConfigurationBuilder<>(YAMLConfiguration.class)
                          .configure(new Parameters().hierarchical().setURL(filePath.toURL()));
        builder.addEventListener(ConfigurationBuilderEvent.CONFIGURATION_REQUEST, (EventListener) event -> {
            builder.getReloadingController().checkForReloading(null);
            LOG.debug("Reloading {}", filePath.toString());
        });
        //Test initial configuration
        builder.getConfiguration();
    }

    /**
     * Provide VES Mapping configuration.
     */
    public VesMappingConfiguration getVesMappingConfiguration() throws ConfigurationException, IOException {
        final YAMLConfiguration configuration = builder.getConfiguration();
        final StringWriter output = new StringWriter();
        configuration.write(output);
        return YAML_READER.readValue(output.toString(), VesMappingConfiguration.class);
    }
}
