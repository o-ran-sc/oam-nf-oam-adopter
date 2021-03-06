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

package org.o.ran.oam.nf.oam.adopter.mock.app.controller;

import static org.o.ran.oam.nf.oam.adopter.mock.app.config.AuthTokenFilter.TOKEN;

import com.google.gson.Gson;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.o.ran.oam.nf.oam.adopter.mock.api.ControllerApi;
import org.o.ran.oam.nf.oam.adopter.mock.app.pojo.TimeZoneOffsetResponse;
import org.o.ran.oam.nf.oam.adopter.mock.app.pojo.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RanController implements ControllerApi {

    private static final Logger LOG = LoggerFactory.getLogger(RanController.class);
    private static final DateTimeFormatter OFFSET_FORMATTER = DateTimeFormatter.ofPattern("xxx");
    private static final Gson GSON = new Gson();

    @Override
    public ResponseEntity<String> authenticateAndGenerateToken() {
        return ResponseEntity.ok(GSON.toJson(new TokenResponse(TOKEN)));
    }

    @Override
    public ResponseEntity<Resource> getPerformanceManagementFiles() {
        LOG.info("Read pm files.");
        final var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(ZipUtil.read(), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getTimeZone() {
        final var zoneId = ZoneId.systemDefault();
        final ZoneOffset offset = zoneId.getRules().getOffset(Instant.now());
        LOG.info("ZoneId {} / Offset {}", zoneId, offset);
        return ResponseEntity.ok(GSON.toJson(new TimeZoneOffsetResponse(OFFSET_FORMATTER.format(offset))));
    }
}
