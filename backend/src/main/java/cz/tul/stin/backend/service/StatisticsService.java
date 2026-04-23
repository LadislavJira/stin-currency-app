package cz.tul.stin.backend.service;

import cz.tul.stin.backend.client.ExchangeRateClient;
import cz.tul.stin.backend.exception.ExternalApiException;
import cz.tul.stin.backend.model.CurrencySymbol;
import cz.tul.stin.backend.model.ExchangeRate;
import cz.tul.stin.backend.model.dto.DashboardResponse;
import cz.tul.stin.backend.model.dto.ExtremesResult;
import cz.tul.stin.backend.model.dto.LiveRatesResponse;
import cz.tul.stin.backend.model.dto.TimeframeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final ExchangeRateClient exchangeRateClient;

    public DashboardResponse getDashboardData(String base, String symbols, String startDate, String endDate) {
        log.info("Zahajuji přípravu dat pro Dashboard. Base: {}, Sledované měny: {}, Období: {} až {}", base, symbols, startDate, endDate);

        validateInputs(base, symbols, startDate, endDate);
        List<String> requestedSymbols = parseSymbols(symbols);

        ExtremesResult extremes = this.findExtremes(base, symbols);

        log.info("Stahuji historická data z API pro výpočet průměrů a grafu");
        TimeframeResponse response = exchangeRateClient.getHistoricalRates(startDate, endDate, base, symbols);

        if (response == null || !response.isSuccess() || response.getQuotes() == null) {
            log.error("Externí API nevrátilo platná historická data pro období {} až {}", startDate, endDate);
            throw new ExternalApiException("Nepodařilo se získat historická data.");
        }

        List<ExchangeRate> domainRates = mapTimeframeToDomain(response, requestedSymbols, startDate, endDate);

        Map<String, Double> averages = calculateAverages(domainRates);
        Map<String, Map<String, Double>> timeseries = buildTimeseries(response, requestedSymbols, startDate, endDate);

        log.info("Dashboard data úspěšně zpracována a odesílána.");
        return DashboardResponse.builder()
                .extremes(extremes)
                .averages(averages)
                .timeseries(timeseries)
                .build();
    }

    public ExtremesResult findExtremes(String base, String symbols) {
        log.info("Zjišťuji aktuální kurzy pro výpočet extrémů");
        LiveRatesResponse response = exchangeRateClient.getLatestRates(base, symbols);

        if (response == null || !response.isSuccess() || response.getQuotes() == null) {
            log.error("Externí API nevrátilo platná aktuální data pro base={}", base);
            throw new ExternalApiException("Nepodařilo se získat data pro výpočet extrémů.");
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

        log.info("Extrémy vypočítány - Nejsilnější: {} ({}), Nejslabší: {} ({})", strongestCurr, strongestVal, weakestCurr, weakestVal);
        return new ExtremesResult(strongestCurr, strongestVal, weakestCurr, weakestVal);
    }


    private void validateInputs(String base, String symbols, String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            if (start.isAfter(end)) {
                log.error("Chyba validace: Počáteční datum {} je po koncovém {}", startDate, endDate);
                throw new IllegalArgumentException("Počáteční datum nemůže být po koncovém datu.");
            }
        } catch (DateTimeParseException e) {
            log.error("Chyba validace: Neplatný formát data", e);
            throw new IllegalArgumentException("Neplatný formát data. Použijte YYYY-MM-DD.");
        }

        if (!CurrencySymbol.isValid(base)) {
            log.error("Chyba validace: Neplatná základní měna '{}'", base);
            throw new IllegalArgumentException("Neplatná základní měna: " + base);
        }

        for (String symbol : parseSymbols(symbols)) {
            if (!CurrencySymbol.isValid(symbol)) {
                log.error("Chyba validace: Neplatná sledovaná měna '{}'", symbol);
                throw new IllegalArgumentException("Neplatná sledovaná měna: " + symbol);
            }
        }
    }

    private Map<String, Double> calculateAverages(List<ExchangeRate> domainRates) {
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
            int count = counts.getOrDefault(currency, 0);
            Double average = entry.getValue() / count;
            averages.put(currency, Math.round(average * 1000.0) / 1000.0);
        }
        return averages;
    }

    private Map<String, Map<String, Double>> buildTimeseries(TimeframeResponse response, List<String> requestedSymbols, String startDate, String endDate) {
        Map<String, Map<String, Double>> timeseries = new TreeMap<>();

        for (var dateEntry : response.getQuotes().entrySet()) {
            String date = dateEntry.getKey();
            if (date.compareTo(startDate) < 0 || date.compareTo(endDate) > 0) {
                continue;
            }

            if (dateEntry.getValue() == null) {
                continue;
            }

            Map<String, Double> dailyCleanedRates = new HashMap<>();
            for (var currencyEntry : dateEntry.getValue().entrySet()) {
                String pair = currencyEntry.getKey();
                if (pair != null && pair.length() == 6) {
                    String targetCurrency = pair.substring(3);
                    if (requestedSymbols.contains(targetCurrency)) {
                        dailyCleanedRates.put(targetCurrency, currencyEntry.getValue());
                    }
                }
            }
            timeseries.put(date, dailyCleanedRates);
        }
        return timeseries;
    }

    private List<String> parseSymbols(String symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return new ArrayList<>();
        }
        return java.util.Arrays.asList(symbols.split(","));
    }

    private List<ExchangeRate> mapTimeframeToDomain(TimeframeResponse dto, List<String> requestedSymbols, String startDate, String endDate) {
        List<ExchangeRate> domainList = new ArrayList<>();
        for (var dateEntry : dto.getQuotes().entrySet()) {
            String date = dateEntry.getKey();
            if (date.compareTo(startDate) < 0 || date.compareTo(endDate) > 0) {
                continue;
            }
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

    private List<ExchangeRate> mapLatestToDomain(LiveRatesResponse dto, List<String> requestedSymbols) {
        List<ExchangeRate> domainList = new ArrayList<>();
        for (Map.Entry<String, Double> entry : dto.getQuotes().entrySet()) {
            ExchangeRate rate = createRateIfValid(entry.getKey(), entry.getValue(), null, requestedSymbols);
            if (rate != null) {
                domainList.add(rate);
            }
        }
        return domainList;
    }

    private ExchangeRate createRateIfValid(String currencyPair, Double value, String date, List<String> requestedSymbols) {
        if (currencyPair == null || currencyPair.length() != 6) {
            return null;
        }
        String targetCurrency = currencyPair.substring(3);
        if (value == null || !CurrencySymbol.isValid(targetCurrency)) {
            return null;
        }
        if (!requestedSymbols.contains(targetCurrency)) {
            return null;
        }
        ExchangeRate rate = new ExchangeRate();
        rate.setCurrency(targetCurrency);
        rate.setRate(value);
        rate.setDate(date);
        return rate;
    }
}