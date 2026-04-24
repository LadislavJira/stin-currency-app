package cz.tul.stin.backend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tul.stin.backend.model.dto.LiveRatesResponse;
import cz.tul.stin.backend.model.dto.TimeframeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Profile("dev")
public class MockExchangeRateClient implements ExchangeRateClient {

    private final ObjectMapper objectMapper;

    @Autowired
    public MockExchangeRateClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LiveRatesResponse getLatestRates(String base, String symbols) {
        try {
            InputStream is = getClass().getResourceAsStream("/mocks/live.json");
            return objectMapper.readValue(is, LiveRatesResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Error loading mock data for latest rates", e);
        }
    }

    @Override
    public TimeframeResponse getHistoricalRates(String startDate, String endDate, String base, String symbols) {
        try {
            InputStream is = getClass().getResourceAsStream("/mocks/timeframe.json");
            return objectMapper.readValue(is, TimeframeResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Error loading mock data for historical rates", e);
        }
    }
}