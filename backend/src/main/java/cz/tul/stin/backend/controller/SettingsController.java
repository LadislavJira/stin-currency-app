package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.UserSettings;
import cz.tul.stin.backend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<UserSettings> getSettings() {
        return ResponseEntity.ok(settingsService.getSettings());
    }

    @PostMapping
    public ResponseEntity<String> saveSettings(@RequestBody UserSettings settings) {
        settingsService.saveSettings(settings);
        return ResponseEntity.ok("Nastavení bylo úspěšně uloženo.");
    }
}