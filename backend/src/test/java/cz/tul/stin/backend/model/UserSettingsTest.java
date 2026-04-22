package cz.tul.stin.backend.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserSettingsTest {

    @Test
    void testUserSettingsDefaultsAndLombok() {
        UserSettings settings = new UserSettings();

        assertEquals("EUR", settings.getBaseCurrency());
        assertEquals(3, settings.getSelectedCurrencies().size());

        settings.setBaseCurrency("USD");
        settings.setSelectedCurrencies(List.of("CZK"));

        assertEquals("USD", settings.getBaseCurrency());
        assertEquals(1, settings.getSelectedCurrencies().size());

        UserSettings settings1 = new UserSettings();
        UserSettings settings2 = new UserSettings();
        assertEquals(settings1, settings2);
        assertEquals(settings1.hashCode(), settings2.hashCode());
        assertNotNull(settings1.toString());
    }
}