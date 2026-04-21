package cz.tul.stin.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeframeResponse {
    private boolean success;
    private boolean timeframe;
    private String source;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    private Map<String, Map<String, Double>> quotes;

    private ApiError error;
}