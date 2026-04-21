package cz.tul.stin.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tul.stin.backend.model.UserSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SettingsService settingsService;

    private Path tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = Files.createTempFile("test-settings", ".json");
        ReflectionTestUtils.setField(settingsService, "settingsFilePath", tempFile.toAbsolutePath().toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testSaveAndGetSettings_Success() {
        UserSettings settingsToSave = new UserSettings();
        settingsToSave.setBaseCurrency("JPY");

        assertDoesNotThrow(() -> settingsService.saveSettings(settingsToSave));

        UserSettings loadedSettings = settingsService.getSettings();
        assertEquals("JPY", loadedSettings.getBaseCurrency());
    }

    @Test
    void testGetSettings_ReturnsDefaultWhenFileDoesNotExist() throws IOException {
        Files.deleteIfExists(tempFile);

        UserSettings loadedSettings = settingsService.getSettings();
        assertNotNull(loadedSettings);
        assertEquals("EUR", loadedSettings.getBaseCurrency());
    }

    @Test
    void testSaveSettings_ThrowsExceptionOnBadPath() {
        ReflectionTestUtils.setField(settingsService, "settingsFilePath", "/nesmyslna/cesta/k/souboru.json");

        UserSettings settings = new UserSettings();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> settingsService.saveSettings(settings));
        assertTrue(exception.getMessage().contains("Nepodařilo se uložit nastavení"));
    }
    @Test
    void testGetSettings_CatchesIOException_WhenFileIsCorrupted() throws IOException {
        Files.writeString(tempFile, "toto urcite neni validni json struktura { [");

        UserSettings loadedSettings = settingsService.getSettings();

        assertNotNull(loadedSettings);
        assertEquals("EUR", loadedSettings.getBaseCurrency());
    }
}