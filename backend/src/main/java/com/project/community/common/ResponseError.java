package com.project.community.common;

import lombok.Data;
import lombok.Getter;

import java.util.Map;

@Data
public class ResponseError {
    private int status;
    private String message;
    private Map<String, String> errors;

    public ResponseError(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }
}
