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

package org.o.ran.oam.nf.oam.adopter.app.controller;

import java.util.List;
import lombok.SneakyThrows;
import org.o.ran.oam.nf.oam.adopter.api.ControllerApi;
import org.o.ran.oam.nf.oam.adopter.model.Adapter;
import org.o.ran.oam.nf.oam.adopter.model.AdapterMechId;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.PerformanceManagementAdaptersDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication(scanBasePackages = {"org.o.ran.oam.adopter"})
@RequestMapping(path = "/adapters")
public class AdapterController implements ControllerApi {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterController.class);

    private final PerformanceManagementAdaptersDeployer deployer;

    @Autowired
    public AdapterController(final PerformanceManagementAdaptersDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    @SneakyThrows
    public ResponseEntity<Void> addAdapter(final Adapter adapter) {
        LOG.info("Request triggered: addAdapter");
        final AdapterMechId mechIdDto = adapter.getMechId();
        deployer.create(adapter.getHost(), mechIdDto.getUsername(), mechIdDto.getPassword());
        return ResponseEntity.ok().build();
    }

    @Override
    @SneakyThrows
    public ResponseEntity<List<String>> getAllAdapters() {
        LOG.info("Request triggered: getAllAdapters");
        return ResponseEntity.ok(deployer.getAll());
    }

    @Override
    @SneakyThrows
    public ResponseEntity<Void> removeAdapter(final String host) {
        LOG.info("Request triggered: removeAdapter");
        deployer.delete(host);
        return ResponseEntity.ok().build();
    }
}
