package cz.tul.stin.backend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import cz.tul.stin.backend.model.dto.TimeseriesResponse;
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
    public LatestRatesResponse getLatestRates(String base, String symbols) {
        try {
            InputStream is = getClass().getResourceAsStream("/mocks/latest.json");
            return objectMapper.readValue(is, LatestRatesResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Chyba při načítání mock dat pro aktuální kurzy", e);
        }
    }
    @Override
    public TimeseriesResponse getHistoricalRates(String startDate, String endDate, String base, String symbols) {
        try {
            InputStream is = getClass().getResourceAsStream("/mocks/timeseries.json");
            return objectMapper.readValue(is, TimeseriesResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Chyba při načítání mock dat pro historii ", e);
        }
    }
}

