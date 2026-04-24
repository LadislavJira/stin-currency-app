package cz.tul.stin.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tul.stin.backend.exception.StorageException;
import cz.tul.stin.backend.model.CurrencySymbol;
import cz.tul.stin.backend.model.UserSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {
    @Value("${settings.file.path:user-settings.json}")
    private String settingsFilePath;

    private final ObjectMapper objectMapper;

    public UserSettings getSettings(){
        File file = new File(settingsFilePath);

        if (file.exists()) {
            try {
                return objectMapper.readValue(file, UserSettings.class);
            } catch (IOException e) {
                log.error("Failed to read settings file. Using default values.", e);
            }
        }
        return new UserSettings();
    }

    public void saveSettings(UserSettings settings) {
        validateSettings(settings);

        try {
            Path path = Paths.get(settingsFilePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(settingsFilePath), settings);
            log.info("User settings successfully saved to: {}", settingsFilePath);

        } catch (IOException e) {
            log.error("Error saving user settings to disk: {}", e.getMessage());
            throw new StorageException("Failed to save settings to disk.", e);
        }
    }

    private void validateSettings(UserSettings settings) {
        if (!CurrencySymbol.isValid(settings.getBaseCurrency())) {
            throw new IllegalArgumentException("error.currency.unsupportedBase");
        }

        if (settings.getSelectedCurrencies() != null) {
            for (String currency : settings.getSelectedCurrencies()) {
                if (!CurrencySymbol.isValid(currency)) {
                    throw new IllegalArgumentException("error.currency.unsupportedTarget");
                }
            }
        }
    }
}