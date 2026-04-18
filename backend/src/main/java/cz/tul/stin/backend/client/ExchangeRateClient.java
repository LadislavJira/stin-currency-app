package cz.tul.stin.backend.client;

import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import cz.tul.stin.backend.model.dto.TimeseriesResponse;

public interface ExchangeRateClient {
    LatestRatesResponse getLatestRates(String base, String symbols);
    TimeseriesResponse getHistoricalRates(String startDate, String endDate, String base, String symbols);
}