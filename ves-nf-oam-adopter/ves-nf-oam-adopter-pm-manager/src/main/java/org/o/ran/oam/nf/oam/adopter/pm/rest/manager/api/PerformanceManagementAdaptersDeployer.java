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

package org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api;

import java.time.ZoneId;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.AlreadyPresentException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.NotFoundException;

public interface PerformanceManagementAdaptersDeployer {

    /**
     * Creates PM Adapter.
     *
     * @param hostIpAddress adapter ip address
     * @param username mechid username
     * @param password mechid password
     * @throws AlreadyPresentException if already present
     */
    void create(@NonNull String hostIpAddress, @NonNull String username, @NonNull String password)
            throws AlreadyPresentException;

    /**
     * Removes PM Adapter by host ip address.
     *
     * @param host ip address
     * @throws NotFoundException if not present
     */
    void delete(@NonNull String host) throws NotFoundException;

    /**
     * Returns list of Adapters host Ip.
     */
    @NonNull List<String> getAll();

    /**
     * Returns time zone of specific device.
     */
    @Nullable ZoneId getTimeZone(@NonNull String host);
}
