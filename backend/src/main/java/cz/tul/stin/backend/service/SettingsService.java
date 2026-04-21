package cz.tul.stin.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tul.stin.backend.model.UserSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

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
                log.error("Nepodařilo se přečíst soubor s nastavením. Použijí se výchozí hodnoty.", e);
            }
        }
        return new UserSettings();
    }

    public void saveSettings(UserSettings settings) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(settingsFilePath), settings);
            log.info("Uživatelské nastavení bylo úspěšně uloženo do souboru.");
        } catch (IOException e) {
            log.error("Chyba při ukládání uživatelského nastavení na disk.", e);
            throw new RuntimeException("Nepodařilo se uložit nastavení na disk.");
        }
    }
}
