package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.ExchangeRate;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        List<ExchangeRate> mockList = new ArrayList<>();
        ExchangeRate rate = new ExchangeRate();
        rate.setCurrency("CZK");
        rate.setRate(25.0);
        mockList.add(rate);

        Mockito.when(currencyService.getFilteredLatestRates("EUR", "CZK")).thenReturn(mockList);

        ResponseEntity<List<ExchangeRate>> response = currencyController.getLatestRates("EUR", "CZK");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CZK", response.getBody().get(0).getCurrency());
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