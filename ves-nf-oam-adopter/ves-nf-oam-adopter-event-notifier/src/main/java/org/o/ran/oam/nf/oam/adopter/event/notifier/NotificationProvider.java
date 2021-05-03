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

package org.o.ran.oam.nf.oam.adopter.event.notifier;

import com.google.gson.Gson;
import io.reactivex.rxjava3.core.Completable;
import java.util.List;
import org.o.ran.oam.nf.oam.adopter.api.CommonEventFormat302ONAP;
import org.o.ran.oam.nf.oam.adopter.api.Event;
import org.o.ran.oam.nf.oam.adopter.api.VesEventNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public final class NotificationProvider implements VesEventNotifier {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationProvider.class);
    private static final Gson GSON = new Gson();
    private final RestVesAgent restVesAgent;

    @Autowired
    public NotificationProvider(final RestVesAgent notificationProvider) {
        this.restVesAgent = notificationProvider;
    }

    @Override
    public Completable notifyEvents(final CommonEventFormat302ONAP event) {
        LOG.debug("Sending VES Messages");
        final String payload = GSON.toJson(event, CommonEventFormat302ONAP.class);
        final List<Event> eventsList = event.getEventList();
        if (eventsList != null && !eventsList.isEmpty()) {
            return restVesAgent.notifyEventBatch(payload);
        }
        return restVesAgent.notifyEvent(payload);
    }
}