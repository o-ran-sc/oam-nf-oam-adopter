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

package org.o.ran.oam.nf.oam.adopter.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptsLogger {
    private static final Logger LOG = LoggerFactory.getLogger(LoginAttemptsLogger.class);

    /**
     *  audit Application Event.
     */
    @EventListener
    public void auditEventHappened(final AuditApplicationEvent auditApplicationEvent) {
        final AuditEvent auditEvent = auditApplicationEvent.getAuditEvent();
        final WebAuthenticationDetails details = (WebAuthenticationDetails) auditEvent.getData().get("details");
        LOG.info("AUDIT: User: {} Event Type: {} Remote IP address: {}",
                auditEvent.getPrincipal(), auditEvent.getType(), details.getRemoteAddress());
    }

    @Bean
    public InMemoryAuditEventRepository auditEventRepository() throws Exception {
        return new InMemoryAuditEventRepository();
    }
}
