package cz.tul.stin.backend.controller;

import cz.tul.stin.backend.model.dto.DashboardResponse;
import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.service.CurrencyService;
import cz.tul.stin.backend.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurrencyService currencyService;

    @MockitoBean
    private StatisticsService statisticsService;

    @Test
    @WithMockUser
    void testGetAvailableSymbols_ReturnsListOfCurrencies() throws Exception {
        List<String> mockSymbols = Arrays.asList("EUR", "CZK", "USD");
        Mockito.when(currencyService.getAvailableSymbols()).thenReturn(mockSymbols);

        mockMvc.perform(get("/api/currencies/symbols")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[1]").value("CZK"));
    }

    @Test
    @WithMockUser
    void testGetDashboardData_WithValidParams_ReturnsDashboardResponse() throws Exception {
        ExtremesResult extremes = new ExtremesResult("CZK", 25.0, "USD", 1.1);
        Map<String, Double> averages = new HashMap<>();
        averages.put("CZK", 24.5);
        Map<String, Map<String, Double>> timeseries = new HashMap<>();

        DashboardResponse mockResponse = DashboardResponse.builder()
                .extremes(extremes)
                .averages(averages)
                .timeseries(timeseries)
                .build();

        Mockito.when(statisticsService.getDashboardData(eq("EUR"), eq("CZK,USD"), eq("2025-01-01"), eq("2025-01-02")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/currencies/dashboard")
                        .param("base", "EUR")
                        .param("symbols", "CZK,USD")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-02")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extremes.strongestCurrency").value("CZK"))
                .andExpect(jsonPath("$.extremes.strongestValue").value(25.0))
                .andExpect(jsonPath("$.averages.CZK").value(24.5));
    }

    @Test
    @WithMockUser
    void testGetDashboardData_WithoutRequiredParams_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/currencies/dashboard")
                        .param("base", "EUR"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGetDashboardData_UsesDefaultBaseCurrency_WhenBaseIsMissing() throws Exception {
        DashboardResponse mockResponse = DashboardResponse.builder().build();

        Mockito.when(statisticsService.getDashboardData(eq("EUR"), eq("CZK"), eq("2025-01-01"), eq("2025-01-01")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/currencies/dashboard")
                        .param("symbols", "CZK")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verify(statisticsService).getDashboardData("EUR", "CZK", "2025-01-01", "2025-01-01");
    }
}