package cz.tul.stin.backend.model.dto;

import lombok.Data;

@Data
public class ApiError {
    private int code;
    private String type;
    private String info;
}