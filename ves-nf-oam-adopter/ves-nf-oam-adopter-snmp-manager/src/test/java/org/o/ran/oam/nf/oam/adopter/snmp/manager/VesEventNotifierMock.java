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

import com.google.gson.Gson;
import io.reactivex.rxjava3.core.Completable;
import org.o.ran.oam.nf.oam.adopter.api.CommonEventFormat302ONAP;
import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.springframework.stereotype.Service;

@Service("test")
class VesEventNotifierMock implements VesEventNotifier {

    private static final Gson GSON = new Gson();
    private CommonEventFormat302ONAP event;

    @Override
    public synchronized Completable notifyEvents(final CommonEventFormat302ONAP event) {
        this.event = event;
        return Completable.complete();
    }

    protected String getEvent() {
        if (event == null) {
            return null;
        }
        return GSON.toJson(event, CommonEventFormat302ONAP.class);
    }

    protected void clear() {
        event = null;
    }
}
