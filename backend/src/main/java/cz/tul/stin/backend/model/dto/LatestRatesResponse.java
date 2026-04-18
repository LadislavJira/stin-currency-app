package cz.tul.stin.backend.model.dto;

import lombok.Data;
import java.util.Map;

@Data
public class LatestRatesResponse {
    private boolean success;
    private Long timestamp;
    private String base;
    private String date;
    private Map<String, Double> rates;
    private ApiError error;
}