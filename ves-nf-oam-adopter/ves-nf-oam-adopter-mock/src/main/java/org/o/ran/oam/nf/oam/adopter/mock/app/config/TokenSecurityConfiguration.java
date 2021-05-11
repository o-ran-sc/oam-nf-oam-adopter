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

package org.o.ran.oam.nf.oam.adopter.mock.app.config;

import org.o.ran.oam.nf.oam.adopter.mock.app.properties.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Order(2)
public class TokenSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final SecurityProperties security;

    @Autowired
    public TokenSecurityConfiguration(final SecurityProperties security) {
        super();
        this.security = security;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.requiresChannel().anyRequest().requiresSecure();
        http.csrf().disable()
                .addFilterAfter(new AuthTokenFilter(security), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/pm").permitAll()
                .antMatchers(HttpMethod.POST, "/system").permitAll()
                .anyRequest()
                .authenticated();
    }
}