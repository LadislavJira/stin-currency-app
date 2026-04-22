package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.ExchangeRate;
import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.service.CurrencyService;
import cz.tul.stin.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;
    private final StatisticsService statisticsService;

    @GetMapping("/latest")
    public ResponseEntity<List<ExchangeRate>> getLatestRates(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam String symbols) {

        List<ExchangeRate> rates = currencyService.getFilteredLatestRates(base, symbols);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/extremes")
    public ResponseEntity<ExtremesResult> getExtremes(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam String symbols) {

        return ResponseEntity.ok(statisticsService.findExtremes(base, symbols));
    }

    @GetMapping("/averages")
    public ResponseEntity<Map<String, Double>> getAverages(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam String symbols,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        return ResponseEntity.ok(statisticsService.calculateAverages(startDate, endDate, base, symbols));
    }
}