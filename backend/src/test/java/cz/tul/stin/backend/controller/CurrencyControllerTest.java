package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.service.CurrencyService;
import cz.tul.stin.backend.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CurrencyControllerTest {

    @Mock
    private CurrencyService currencyService;

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private CurrencyController currencyController;

    @Test
    void testGetLatestRates() {
        Map<String, Double> mockMap = new HashMap<>();
        mockMap.put("CZK", 25.0);

        Mockito.when(currencyService.getFilteredLatestRates("EUR")).thenReturn(mockMap);

        ResponseEntity<Map<String, Double>> response = currencyController.getLatestRates("EUR");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(25.0, response.getBody().get("CZK"));
    }

    @Test
    void testGetExtremes() {
        ExtremesResult mockResult = new ExtremesResult("CZK", 25.0, "USD", 1.1);

        Mockito.when(statisticsService.findExtremes("EUR", "CZK,USD")).thenReturn(mockResult);

        ResponseEntity<ExtremesResult> response = currencyController.getExtremes("EUR", "CZK,USD");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CZK", response.getBody().getStrongestCurrency());
    }

    @Test
    void testGetAverages() {
        Map<String, Double> mockMap = new HashMap<>();
        mockMap.put("CZK", 24.5);

        Mockito.when(statisticsService.calculateAverages("2025-01-01", "2025-01-03", "EUR", "CZK"))
                .thenReturn(mockMap);

        ResponseEntity<Map<String, Double>> response = currencyController.getAverages("EUR", "CZK", "2025-01-01", "2025-01-03");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(24.5, response.getBody().get("CZK"));
    }
}