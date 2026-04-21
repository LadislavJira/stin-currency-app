package cz.tul.stin.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveRatesResponse {
    private boolean success;
    private Long timestamp;
    private String source;
    private String date;
    private Map<String, Double> quotes;
    private ApiError error;
}