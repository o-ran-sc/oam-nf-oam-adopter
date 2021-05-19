package org.o.ran.oam.nf.oam.adopter.app;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.Gson;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.o.ran.oam.nf.oam.adopter.model.Adapter;
import org.o.ran.oam.nf.oam.adopter.model.AdapterMechId;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.PerformanceManagementMapperConfigProvider;
import org.o.ran.oam.nf.oam.adopter.pm.rest.manager.api.PerformanceManagementAdaptersDeployer;
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
        mockMvc = MockMvcBuilders
                      .webAppContextSetup(context)
                      .apply(springSecurity())
                      .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testGetAllAdapters() throws Exception {
        when(deployer.getAll()).thenReturn(Collections.singletonList("mockResult"));

        mockMvc
                .perform(get("/adapters/")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("mockResult")));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void testDeleteAdapter() throws Exception {
        mockMvc
                .perform(delete("/adapters/adapter/172.10.55.3")
                                 .contentType(MediaType.APPLICATION_JSON))
                .andDo(print()).andExpect(status().isOk());
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

        mockMvc.perform(post("/adapters/adapter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(GSON.toJson(adapter)))
                .andDo(print()).andExpect(status().isOk());
    }
}