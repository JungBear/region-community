package com.project.community.common;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int status;
    private String message;
    private T data;

    public ApiResponse(ResponseCode responseCode, T data) {
        this.status = responseCode.getStatus();
        this.message = responseCode.getMessage();
        this.data = data;
    }
}
