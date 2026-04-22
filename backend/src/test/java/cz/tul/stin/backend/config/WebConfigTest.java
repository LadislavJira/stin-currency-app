package cz.tul.stin.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistration;

import static org.mockito.Mockito.*;

class WebConfigTest {

    @Test
    void testCorsMappings() {
        WebConfig webConfig = new WebConfig();
        CorsRegistry registryMock = mock(CorsRegistry.class);

        CorsRegistration registrationMock = mock(CorsRegistration.class, RETURNS_SELF);

        when(registryMock.addMapping("/api/**")).thenReturn(registrationMock);

        webConfig.addCorsMappings(registryMock);

        // Ověříme, že se všechno správně nastavilo
        verify(registryMock).addMapping("/api/**");
        verify(registrationMock).allowedOrigins("http://localhost:3000");
        verify(registrationMock).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
        verify(registrationMock).allowedHeaders("*");
        verify(registrationMock).allowCredentials(true);
    }
}