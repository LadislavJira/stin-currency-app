package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.model.dto.HistoryResponse;
import cz.tul.stin.backend.service.CurrencyService;
import cz.tul.stin.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Slf4j
public class CurrencyController {

    private final CurrencyService currencyService;
    private final StatisticsService statisticsService;

    @GetMapping("/symbols")
    public ResponseEntity<List<String>> getAvailableSymbols() {
        log.info("Received HTTP GET request for /api/currencies/symbols");
        List<String> symbols = currencyService.getAvailableSymbols();
        return ResponseEntity.ok(symbols);
    }

    @GetMapping("/extremes")
    public ResponseEntity<ExtremesResult> getExtremes(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam String symbols) {

        log.info("Received HTTP GET request for /api/currencies/extremes with base={}, symbols={}", base, symbols);

        ExtremesResult response = statisticsService.getExtremes(base, symbols);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<HistoryResponse> getHistory(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam String symbols,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.info("Received HTTP GET request for /api/currencies/history with base={}, symbols={}, startDate={}, endDate={}",
                base, symbols, startDate, endDate);

        HistoryResponse response = statisticsService.getHistory(base, symbols, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}