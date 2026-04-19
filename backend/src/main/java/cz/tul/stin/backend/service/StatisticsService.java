package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.model.CurrencySymbol;
import cz.tul.stin.backend.model.ExchangeRate;
import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.model.dto.LatestRatesResponse;
import cz.tul.stin.backend.model.dto.TimeseriesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        List<String> requestedSymbols = parseSymbols(symbols);
        List<ExchangeRate> domainRates = mapLatestToDomain(response, requestedSymbols);

        String strongestCurr = null;
        double strongestVal = Double.MIN_VALUE;
        String weakestCurr = null;
        double weakestVal = Double.MAX_VALUE;

        for (ExchangeRate rateObj : domainRates) {
            String currency = rateObj.getCurrency();
            double rate = rateObj.getRate();

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

        List<String> requestedSymbols = parseSymbols(symbols);
        List<ExchangeRate> domainRates = mapTimeseriesToDomain(response, requestedSymbols);

        Map<String, Double> sums = new HashMap<>();
        Map<String, Integer> counts = new HashMap<>();

        for (ExchangeRate rate : domainRates) {
            String currency = rate.getCurrency();
            sums.put(currency, sums.getOrDefault(currency, 0.0) + rate.getRate());
            counts.put(currency, counts.getOrDefault(currency, 0) + 1);
        }

        Map<String, Double> averages = new HashMap<>();
        for (Map.Entry<String, Double> entry : sums.entrySet()) {
            String currency = entry.getKey();
            Double average = entry.getValue() / counts.get(currency);
            averages.put(currency, Math.round(average * 1000.0) / 1000.0);
        }


        return averages;
    }

    private List<String> parseSymbols(String symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return null;
        }
        return java.util.Arrays.asList(symbols.split(","));
    }

    private List<ExchangeRate> mapTimeseriesToDomain(TimeseriesResponse dto, List<String> requestedSymbols) {
        List<ExchangeRate> domainList = new ArrayList<>();

        for (var dateEntry : dto.getRates().entrySet()) {
            String date = dateEntry.getKey();
            Map<String, Double> dailyRates = dateEntry.getValue();

            if (dailyRates == null) continue;

            for (var currencyEntry : dailyRates.entrySet()) {
                ExchangeRate rate = createRateIfValid(currencyEntry.getKey(), currencyEntry.getValue(), date, requestedSymbols);

                if (rate != null) {
                    domainList.add(rate);
                }
            }
        }
        return domainList;
    }

    private List<ExchangeRate> mapLatestToDomain(LatestRatesResponse dto, List<String> requestedSymbols) {
        List<ExchangeRate> domainList = new ArrayList<>();

        for (Map.Entry<String, Double> entry : dto.getRates().entrySet()) {
            ExchangeRate rate = createRateIfValid(entry.getKey(), entry.getValue(), null, requestedSymbols);

            if (rate != null) {
                domainList.add(rate);
            }
        }
        return domainList;
    }

    private ExchangeRate createRateIfValid(String currency, Double value, String date, List<String> requestedSymbols) {
        if (value == null || !CurrencySymbol.isValid(currency)) {
            return null;
        }
        if (requestedSymbols != null && !requestedSymbols.contains(currency)) {
            return null;
        }

        ExchangeRate rate = new ExchangeRate();
        rate.setCurrency(currency);
        rate.setRate(value);
        rate.setDate(date);

        return rate;
    }
}
