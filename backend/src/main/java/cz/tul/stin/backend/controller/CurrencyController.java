package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.dto.DashboardResponse;
import cz.tul.stin.backend.service.CurrencyService;
import cz.tul.stin.backend.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/currencies")
@RequiredArgsConstructor
@Slf4j
public class CurrencyController {

    private final CurrencyService currencyService;
    private final StatisticsService statisticsService;

    @GetMapping("/symbols")
    public ResponseEntity<List<String>> getAvailableSymbols() {
        log.info("Přijat HTTP GET požadavek na /api/currencies/symbols");

        List<String> symbols = currencyService.getAvailableSymbols();

        return ResponseEntity.ok(symbols);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardData(
            @RequestParam(defaultValue = "EUR") String base,
            @RequestParam String symbols,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        log.info("Přijat HTTP GET požadavek na /api/currencies/dashboard");
        DashboardResponse response = statisticsService.getDashboardData(base, symbols, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}