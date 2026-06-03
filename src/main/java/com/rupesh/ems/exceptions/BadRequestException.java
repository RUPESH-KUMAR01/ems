package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(Response.Status.BAD_REQUEST, message, "BAD_REQUEST");
    }

    public BadRequestException(String message, Throwable cause) {
        super(Response.Status.BAD_REQUEST, message, "BAD_REQUEST", cause);
    }
}