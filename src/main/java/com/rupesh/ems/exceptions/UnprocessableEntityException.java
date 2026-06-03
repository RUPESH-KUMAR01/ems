package com.rupesh.ems.exceptions;

import jakarta.ws.rs.core.Response;

public class UnprocessableEntityException extends ApiException {
    private static final Response.StatusType UNPROCESSABLE_ENTITY = new Response.StatusType() {
        @Override
        public int getStatusCode() {
            return 422;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.CLIENT_ERROR;
        }

        @Override
        public String getReasonPhrase() {
            return "Unprocessable Entity";
        }
    };

    public UnprocessableEntityException(String message) {
        super(UNPROCESSABLE_ENTITY, message, "UNPROCESSABLE_ENTITY");
    }

    public UnprocessableEntityException(String message, Throwable cause) {
        super(UNPROCESSABLE_ENTITY, message, "UNPROCESSABLE_ENTITY", cause);
    }
}