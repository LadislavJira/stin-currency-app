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
import java.util.List;

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
        settingsToSave.setSelectedCurrencies(List.of("USD", "EUR"));

        assertDoesNotThrow(() -> settingsService.saveSettings(settingsToSave));

        UserSettings loadedSettings = settingsService.getSettings();
        assertEquals("JPY", loadedSettings.getBaseCurrency());
    }

    @Test
    void testGetSettings_ReturnsDefaultWhenFileDoesNotExist() throws IOException {
        Files.deleteIfExists(tempFile);

        UserSettings loadedSettings = settingsService.getSettings();
        assertNotNull(loadedSettings);
        assertEquals("EUR", loadedSettings.getBaseCurrency()); // Předpokládám, že výchozí je EUR
    }

    @Test
    void testSaveSettings_ThrowsExceptionOnBadPath() throws IOException {
        File dummyFile = File.createTempFile("dummy", ".txt");
        dummyFile.deleteOnExit();
        String guaranteedBadPath = dummyFile.getAbsolutePath() + "/soubor.json";
        ReflectionTestUtils.setField(settingsService, "settingsFilePath", guaranteedBadPath);

        UserSettings settings = new UserSettings();
        settings.setBaseCurrency("EUR");

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


    @Test
    void testSaveSettings_ThrowsExceptionOnInvalidBaseCurrency() {
        UserSettings settings = new UserSettings();
        settings.setBaseCurrency("ZEME");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> settingsService.saveSettings(settings));
        assertTrue(exception.getMessage().contains("Nepodporovaná základní měna"));
    }

    @Test
    void testSaveSettings_ThrowsExceptionOnInvalidCurrencyInList() {
        UserSettings settings = new UserSettings();
        settings.setBaseCurrency("EUR");
        settings.setSelectedCurrencies(List.of("CZK", "NEEXISTUJE", "USD"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> settingsService.saveSettings(settings));
        assertTrue(exception.getMessage().contains("Nepodporovaná měna v seznamu"));
    }

    @Test
    void testSaveSettings_SuccessWithNullSelectedCurrencies() {
        UserSettings settings = new UserSettings();
        settings.setBaseCurrency("EUR");
        settings.setSelectedCurrencies(null);

        assertDoesNotThrow(() -> settingsService.saveSettings(settings));
    }

    @Test
    void testSaveSettings_SuccessWithNoParentDirectory() throws IOException {
        String noParentPath = "test-pouze-jmeno-souboru.json";
        ReflectionTestUtils.setField(settingsService, "settingsFilePath", noParentPath);

        UserSettings settings = new UserSettings();
        settings.setBaseCurrency("CZK");

        try {
            assertDoesNotThrow(() -> settingsService.saveSettings(settings));
            assertTrue(Files.exists(Path.of(noParentPath)));
        } finally {
            Files.deleteIfExists(Path.of(noParentPath));
        }
    }
}