package com.avito.merchshop.model.response.exception;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
public class ExceptionResponse {
    String errors;
    public ExceptionResponse(String errors) {
        this.errors = errors;
    }
}
