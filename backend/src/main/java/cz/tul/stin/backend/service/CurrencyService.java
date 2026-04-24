package cz.tul.stin.backend.service;


import cz.tul.stin.backend.model.CurrencySymbol;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.Arrays;
import java.util.List;

import java.util.stream.Collectors;

@Service
@Slf4j
public class CurrencyService {
    public List<String> getAvailableSymbols() {
        log.info("Building list of available currencies from enum.");
        return Arrays.stream(CurrencySymbol.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}