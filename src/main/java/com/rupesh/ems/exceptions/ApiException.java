package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public abstract class ApiException extends RuntimeException {
    private final Response.StatusType status;
    private final String errorCode;

    protected ApiException(Response.StatusType status, String message) {
        this(status, message, null, null);
    }

    protected ApiException(Response.StatusType status, String message, String errorCode) {
        this(status, message, errorCode, null);
    }

    protected ApiException(Response.StatusType status, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public Response.StatusType getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}