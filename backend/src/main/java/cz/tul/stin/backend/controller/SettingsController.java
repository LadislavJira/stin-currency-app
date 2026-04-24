package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.UserSettings;
import cz.tul.stin.backend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Slf4j
public class SettingsController {

    private final SettingsService settingsService;
    private final MessageSource messageSource;

    @GetMapping
    public ResponseEntity<UserSettings> getSettings() {
        log.info("Received HTTP GET request for /api/settings");

        UserSettings settings = settingsService.getSettings();
        log.debug("Returning user settings: baseCurrency={}, selectedCurrencies={}",
                settings.getBaseCurrency(), settings.getSelectedCurrencies());

        return ResponseEntity.ok(settings);
    }

    @PostMapping
    public ResponseEntity<String> saveSettings(@RequestBody UserSettings settings) {
        log.info("Received HTTP POST request for /api/settings with payload: baseCurrency={}, selectedCurrencies={}",
                settings.getBaseCurrency(), settings.getSelectedCurrencies());

        settingsService.saveSettings(settings);

        String successMessage = messageSource.getMessage("success.settings.saved", null, LocaleContextHolder.getLocale());

        log.info("Settings saved successfully.");
        return ResponseEntity.ok(successMessage);
    }
}