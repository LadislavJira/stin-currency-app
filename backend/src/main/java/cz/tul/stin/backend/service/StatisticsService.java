package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.model.CurrencySymbol;
import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import cz.tul.stin.backend.model.dto.TimeseriesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final ExchangeRateClient exchangeRateClient;

    public ExtremesResult findExtremes(String base, String symbols) {

        LatestRatesResponse response = exchangeRateClient.getLatestRates(base, symbols);

        if (response == null || !response.isSuccess() || response.getRates() == null) {
            throw new RuntimeException("Nepodařilo se získat aktuální data");
        }

        String strongestCurr = null;
        double strongestVal = Double.MIN_VALUE;
        String weakestCurr = null;
        double weakestVal = Double.MAX_VALUE;

        for (Map.Entry<String, Double> entry : response.getRates().entrySet()) {
            String currency = entry.getKey();
            Double rate = entry.getValue();

            if (rate == null || !CurrencySymbol.isValid(currency)) continue;

            if (rate > strongestVal) {
                strongestVal = rate;
                strongestCurr = currency;
            }
            if (rate < weakestVal) {
                weakestVal = rate;
                weakestCurr = currency;
            }
        }

        return new ExtremesResult(strongestCurr, strongestVal, weakestCurr, weakestVal);
    }

    public Map<String, Double> calculateAverages(String startDate, String endDate, String base, String symbols) {

        TimeseriesResponse response = exchangeRateClient.getHistoricalRates(startDate, endDate, base, symbols);

        if (response == null || !response.isSuccess() || response.getRates() == null) {
            throw new RuntimeException("Nepodařilo se získat historická data");
        }

        Map<String, Double> sums = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        for (Map.Entry<String, Map<String, Double>> dateEntry : response.getRates().entrySet()) {
            Map<String, Double> dailyRates = dateEntry.getValue();

            if (dailyRates == null || dailyRates.isEmpty()) continue;

            for (Map.Entry<String, Double> rateEntry : dailyRates.entrySet()) {
                String currency = rateEntry.getKey();
                Double rate = rateEntry.getValue();

                if (rate == null || !CurrencySymbol.isValid(currency)) continue;

                sums.put(currency, sums.getOrDefault(currency, 0.0) + rate);
                counts.put(currency, counts.getOrDefault(currency, 0) + 1);
            }
        }

        Map<String, Double> averages = new HashMap<>();
        for (String currency : sums.keySet()) {
            double rawAvg = sums.get(currency) / counts.get(currency);
            averages.put(currency, Math.round(rawAvg * 10000.0) / 10000.0);
        }


        return averages;
    }
}
