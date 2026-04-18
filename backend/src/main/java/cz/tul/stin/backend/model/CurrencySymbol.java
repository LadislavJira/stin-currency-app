package cz.tul.stin.backend.model;

import lombok.Getter;

@Getter
public enum CurrencySymbol {
    AUD, CAD, CHF, CNY, DKK, EUR, GBP, HKD, HUF, ILS,
    JPY, KRW, MXN, NOK, NZD, PLN, RON, SEK, SGD, THB,
    TRY, USD, ZAR, CZK;

    public static boolean isValid(String code) {
        if (code == null) return false;
        for (CurrencySymbol symbol : values()) {
            if (symbol.name().equalsIgnoreCase(code)) return true;
        }
        return false;
    }
}
