package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.UserSettings;
import cz.tul.stin.backend.service.SettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SettingsControllerTest {

    @Mock
    private SettingsService settingsService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private SettingsController settingsController;

    @Test
    void testGetSettings() {
        UserSettings mockSettings = new UserSettings();
        Mockito.when(settingsService.getSettings()).thenReturn(mockSettings);

        ResponseEntity<UserSettings> response = settingsController.getSettings();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSettings, response.getBody());
    }

    @Test
    void testSaveSettings() {
        UserSettings newSettings = new UserSettings();

        Mockito.when(messageSource.getMessage(Mockito.eq("success.settings.saved"), Mockito.any(), Mockito.any()))
                .thenReturn("Nastavení bylo úspěšně uloženo.");

        ResponseEntity<String> response = settingsController.saveSettings(newSettings);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Nastavení bylo úspěšně uloženo.", response.getBody());
        Mockito.verify(settingsService).saveSettings(newSettings);
    }
}