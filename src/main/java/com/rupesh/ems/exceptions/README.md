# Exceptions

This package contains the custom API exceptions used by the service. Each concrete
exception extends `ApiException`, carries an HTTP status, and is converted by
`ApiExceptionMapper` into a JSON `ApiErrorResponse`.

Error responses include:

- `timestamp`
- `status`
- `error`
- `code`
- `message`
- `path`
- `details`

Use clear, client-safe messages when throwing these exceptions. If you are wrapping
a lower-level failure, use the constructor that accepts a `Throwable cause`.

## Quick reference

| Exception | HTTP status | Error code | Use when |
| --- | ---: | --- | --- |
| `ApiException` | Depends on subclass | Depends on subclass | Base class only. Extend it when adding a new API-specific exception. Do not throw it directly. |
| `BadRequestException` | 400 | `BAD_REQUEST` | The request is malformed or has invalid input that prevents the server from understanding what the client wants. |
| `ValidationException` | 400 | `VALIDATION_ERROR` | Request fields fail validation rules and you want to return one or more validation details. |
| `UnauthorizedException` | 401 | `UNAUTHORIZED` | Authentication is missing, expired, or invalid. |
| `ForbiddenException` | 403 | `FORBIDDEN` | The user is authenticated, but does not have permission to perform the action. |
| `NotFoundException` | 404 | `NOT_FOUND` | A requested resource does not exist or should not be exposed to this caller. |
| `ConflictException` | 409 | `CONFLICT` | The request conflicts with an existing resource or the current state of the system. |
| `UnprocessableEntityException` | 422 | `UNPROCESSABLE_ENTITY` | The request is valid in shape, but cannot be processed because it violates business rules. |
| `TooManyRequestsException` | 429 | `TOO_MANY_REQUESTS` | The client has exceeded a rate limit, retry limit, or throttling rule. |
| `InternalServerException` | 500 | `INTERNAL_SERVER_ERROR` | An unexpected server-side failure happened and should be treated as a bug or infrastructure problem. |
| `ServiceUnavailableException` | 503 | `SERVICE_UNAVAILABLE` | A required dependency or service is temporarily unavailable. |

## When to use each exception

### `ApiException`

Use as the base class for custom HTTP exceptions.

Scenarios:

- Add a new API exception with a status not already represented in this package.
- Keep error response formatting consistent through `ApiExceptionMapper`.

Do not throw `ApiException` directly because it is abstract and does not describe a
specific client-facing failure.

### `BadRequestException`

Use when the request is invalid at the protocol or input-shape level.

Scenarios:

- A query parameter has an unsupported value, such as an unknown sort field.
- A path parameter cannot be parsed into the expected type.
- Pagination inputs are invalid, such as `page` less than 1 or `limit` above the allowed maximum.
- A request combines parameters that are not allowed together.

Prefer `ValidationException` when you need to return field-level validation details.
Prefer `UnprocessableEntityException` when the request shape is valid but the
business action is not allowed.

### `ValidationException`

Use when request data fails validation rules and the client should receive a list
of violations in `details`.

Scenarios:

- Required fields are missing during manual validation.
- A password fails custom complexity rules.
- An event request has multiple invalid fields, such as a blank title and a past start date.
- A create or update request fails validation that cannot be expressed cleanly with annotations.

Bean Validation failures from `@Valid` are already handled by
`ConstraintViolationExceptionMapper`. Use this exception for explicit validation
inside your own service or resource code.

Example:

```java
throw new ValidationException("Invalid user request", List.of(
        "email: must be a valid email address",
        "password: must contain at least 8 characters"));
```

### `UnauthorizedException`

Use when authentication failed or was not provided.

Scenarios:

- The `Authorization` header is missing.
- A JWT token is expired, malformed, or cannot be verified.
- Login credentials are invalid.
- A protected endpoint is called before the user is authenticated.

Do not use this when the user is logged in but lacks permission. Use
`ForbiddenException` for that.

### `ForbiddenException`

Use when the caller is authenticated but not allowed to perform the action.

Scenarios:

- A regular user calls an admin-only endpoint.
- A user tries to update or delete another user's resource.
- A user has a valid token but lacks the required role.
- A user tries to access a feature that their account is not allowed to use.

### `NotFoundException`

Use when the requested resource cannot be found.

Scenarios:

- No user exists for the requested user ID.
- No event exists for the requested event ID.
- A ticket, booking, or registration record is missing.
- You intentionally hide a resource from callers who should not know it exists.

### `ConflictException`

Use when the request is valid, but applying it would conflict with current system
state or an existing resource.

Scenarios:

- A user tries to register with an email that already exists.
- A resource is updated with a stale version number.
- A duplicate event slug, ticket code, or booking reference is submitted.
- A booking is attempted for a seat that has just been taken.

This exception is already used in `UserResource` when a registration email already
exists.

### `UnprocessableEntityException`

Use when the request is syntactically valid and well formed, but the requested
operation violates business rules.

Scenarios:

- A user tries to create an event with an end time before the start time.
- A booking is requested after registration has closed.
- A refund is requested outside the refund window.
- A user attempts an action that is logically impossible for the current resource state.

Use `ConflictException` instead when the problem is specifically a clash with
existing state or another resource.

### `TooManyRequestsException`

Use when a caller has exceeded a limit and should slow down.

Scenarios:

- Too many login attempts in a short period.
- Too many password reset requests.
- API rate limit exceeded for a user, IP, or token.
- A retry limit for an action has been reached.

### `InternalServerException`

Use when the server hits an unexpected failure that is not caused by the client.

Scenarios:

- A database call fails unexpectedly.
- Password hashing fails because of an internal error.
- Required server configuration is missing after startup.
- An unexpected exception must be wrapped in a consistent API response.

Avoid exposing sensitive internal details in the message. Log the original cause
and return a safe message to the client.

### `ServiceUnavailableException`

Use when the server cannot complete the request because a dependency is
temporarily unavailable.

Scenarios:

- The database connection pool is unavailable.
- An email, payment, notification, or queue service is down.
- A downstream API times out or returns a temporary outage response.
- The application is temporarily unable to serve traffic during maintenance.

Use this for temporary dependency or availability problems. Use
`InternalServerException` for unexpected application bugs.

## Choosing between similar exceptions

| Situation | Recommended exception |
| --- | --- |
| Invalid request format or unsupported parameter | `BadRequestException` |
| One or more field validation failures | `ValidationException` |
| Missing or invalid authentication | `UnauthorizedException` |
| Authenticated but not allowed | `ForbiddenException` |
| Resource ID does not exist | `NotFoundException` |
| Duplicate or stale state conflict | `ConflictException` |
| Business rule prevents the action | `UnprocessableEntityException` |
| Client is sending too many requests | `TooManyRequestsException` |
| Unexpected application failure | `InternalServerException` |
| Temporary dependency outage | `ServiceUnavailableException` |
