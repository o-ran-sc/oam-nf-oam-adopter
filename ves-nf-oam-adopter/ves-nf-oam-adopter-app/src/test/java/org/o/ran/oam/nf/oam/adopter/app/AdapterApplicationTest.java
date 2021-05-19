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

package org.o.ran.oam.nf.oam.adopter.app;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import java.time.ZoneId;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.o.ran.oam.nf.oam.adopter.app.controller.TimeZoneServiceProvider;
import org.o.ran.oam.nf.oam.adopter.model.Adapter;
import org.o.ran.oam.nf.oam.adopter.model.AdapterMechId;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.PerformanceManagementMapperConfigProvider;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.PerformanceManagementAdaptersDeployer;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.AlreadyPresentException;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.exceptions.NotFoundException;
import org.o.ran.oam.nf.oam.adopter.snmp.manager.SnmpMappingConfigurationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AdapterApplicationTest {

    private static final Gson GSON = new Gson();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TimeZoneServiceProvider timeZoneServiceProvider;
    @MockBean
    private PerformanceManagementAdaptersDeployer deployer;
    @MockBean
    private SnmpMappingConfigurationProvider snmpProvider;
    @MockBean
    private PerformanceManagementMapperConfigProvider pmProvider;
    @Autowired
    private WebApplicationContext context;


    @BeforeEach
    public void applySecurity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testGetAllAdapters() throws Exception {
        when(deployer.getAll()).thenReturn(Collections.singletonList("mockResult"));

        mockMvc.perform(get("/adapters/").secure(true).contentType(MediaType.APPLICATION_JSON)).andDo(print())
                .andExpect(status().isOk()).andExpect(content().string(containsString("mockResult")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testDeleteAdapter() throws Exception {
        mockMvc.perform(delete("/adapters/adapter/172.10.55.3").secure(true).contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testNotFound() throws Exception {
        doThrow(NotFoundException.class).when(deployer).delete(anyString());

        mockMvc.perform(delete("/adapters/adapter/172.10.55.3").secure(true).contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testAddAdapter() throws Exception {

        final Adapter adapter = new Adapter();
        adapter.setHost("172.10.55.3");

        final AdapterMechId mechId = new AdapterMechId();
        mechId.username("admin");
        mechId.password("somePass");
        adapter.setMechId(mechId);

        mockMvc.perform(post("/adapters/adapter").secure(true).contentType(MediaType.APPLICATION_JSON)
                                .content(GSON.toJson(adapter))).andDo(print()).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testAlreadyExist() throws Exception {

        final Adapter adapter = new Adapter();
        adapter.setHost("172.10.55.3");

        final AdapterMechId mechId = new AdapterMechId();
        mechId.username("admin");
        mechId.password("somePass");
        adapter.setMechId(mechId);

        doThrow(AlreadyPresentException.class).when(deployer).create(anyString(), anyString(), anyString());

        mockMvc.perform(post("/adapters/adapter").secure(true).contentType(MediaType.APPLICATION_JSON)
                                .content(GSON.toJson(adapter))).andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testMissingArguments() throws Exception {

        final Adapter adapter = new Adapter();
        adapter.setHost("172.10.55.3");

        final AdapterMechId mechId = new AdapterMechId();
        mechId.username("admin");
        adapter.setMechId(mechId);


        mockMvc.perform(post("/adapters/adapter").secure(true).contentType(MediaType.APPLICATION_JSON)
                                .content(GSON.toJson(adapter))).andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    public void test() {
        final ZoneId zoneId = ZoneId.of("+02:00");
        when(deployer.getTimeZone("172.10.55.3")).thenReturn(zoneId);
        assertEquals(zoneId, timeZoneServiceProvider.getTimeZone("172.10.55.3"));
    }
}