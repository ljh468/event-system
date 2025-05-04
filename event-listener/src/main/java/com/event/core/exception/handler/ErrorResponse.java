package com.event.core.exception.handler;

import com.event.core.exception.ErrorType;

public record ErrorResponse(
    String errorMessage,
    ErrorType errorType) {
}
