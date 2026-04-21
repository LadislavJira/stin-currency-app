package cz.tul.stin.backend.client;

import cz.tul.stin.backend.model.dto.LiveRatesResponse;
import cz.tul.stin.backend.model.dto.TimeframeResponse;

public interface ExchangeRateClient {
    LiveRatesResponse getLatestRates(String base, String symbols);
    TimeframeResponse getHistoricalRates(String startDate, String endDate, String base, String symbols);
}