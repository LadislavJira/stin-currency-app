package cz.tul.stin.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistration;

import static org.mockito.Mockito.*;

class WebConfigTest {

    @Test
    void testCorsMappings_ShouldAllowNecessaryFrontendSettings() {
        WebConfig webConfig = new WebConfig();
        CorsRegistry registryMock = mock(CorsRegistry.class);
        CorsRegistration registrationMock = mock(CorsRegistration.class, RETURNS_SELF);
        when(registryMock.addMapping(anyString())).thenReturn(registrationMock);
        webConfig.addCorsMappings(registryMock);
        verify(registryMock).addMapping(anyString());
        verify(registrationMock).allowedOrigins("http://localhost:5173");
        verify(registrationMock).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
        verify(registrationMock).allowedHeaders("*");
        verify(registrationMock).allowCredentials(true);
    }
}