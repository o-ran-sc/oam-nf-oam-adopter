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

package org.o.ran.oam.nf.oam.adopter.snmp.manager.mapper;

import io.reactivex.rxjava3.core.Maybe;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.o.ran.oam.nf.oam.adopter.api.CommonEventFormat302ONAP;
import org.snmp4j.PDU;
import org.snmp4j.smi.UdpAddress;

public interface SnmpMapper {
    /**
     * Translate SNMP protocol data unit to ONAP Event.
     *
     * @param peerAddress sender address
     * @param timeZone    sender time zone in UTC Format (E.g. UTC+02:00
     * @param pdu         SNMP protocol data unit
     * @return event or null if translation not supported
     */
    @NonNull Maybe<CommonEventFormat302ONAP> toEvent(
            @NonNull UdpAddress peerAddress,
            @Nullable String timeZone,
            @NonNull PDU pdu);
}
