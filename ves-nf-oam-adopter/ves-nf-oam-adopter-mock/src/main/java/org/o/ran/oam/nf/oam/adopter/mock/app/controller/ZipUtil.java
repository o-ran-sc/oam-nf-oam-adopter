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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

@UtilityClass
public final class ZipUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ZipUtil.class);
    private static final String ZIP = "pmFiles.zip";

    /**
     * Read Zip File.
     */
    public static Resource read() {
        try {
            return new InputStreamResource(new FileInputStream(new File(new File("."), ZIP)));
        } catch (final FileNotFoundException e) {
            LOG.error("Failed to load file.", e);
            return null;
        }
    }
}
