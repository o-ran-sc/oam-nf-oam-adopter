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

import org.o.ran.oam.nf.oam.adopter.app.ServerProperties;
import org.o.ran.oam.nf.oam.adopter.app.SslProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableConfigurationProperties
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private static final String ADMIN_ROLE = "ADMIN";
    private final ServerProperties properties;

    @Autowired
    public SecurityConfiguration(final ServerProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final SslProperties ssl = properties.getSsl();
        if (ssl != null && ssl.getEnabled() != null && ssl.getEnabled()) {
            http.requiresChannel().anyRequest().requiresSecure();
        }
        http.csrf().disable().antMatcher("/adapters/**").authorizeRequests().anyRequest().hasRole(ADMIN_ROLE).and()
                .httpBasic();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Autowired
    public void configureGlobal(final AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser(properties.getUsername()).password("{noop}" + properties.getPassword())
                .roles(ADMIN_ROLE);
    }
}
