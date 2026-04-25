package cz.tul.stin.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "API_USERNAME=testAdmin",
        "API_PASSWORD=testPassword123",
        "EXCHANGE_API_KEY=fakeTestKey"
})
@AutoConfigureMockMvc
@ActiveProfiles("prod")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Value("${spring.security.user.name}")
    private String configuredUsername;

    @Value("${spring.security.user.password}")
    private String configuredPassword;

    @Test
    void accessProtectedEndpointWithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/settings"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessProtectedEndpointWithAuth_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/settings")
                        .with(httpBasic(configuredUsername, configuredPassword)))
                .andExpect(status().isOk());
    }
}