package cz.tul.stin.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class TimeseriesResponse {
    private boolean success;
    private boolean timeseries;
    private String base;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    private Map<String, Map<String, Double>> rates;

    private ApiError error;
}