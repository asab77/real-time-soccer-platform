package com.example.soccerplatform.exception;

import com.example.soccerplatform.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DuplicateLeaguePreferenceException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            DuplicateLeaguePreferenceException exception
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .getFirst()
                .getDefaultMessage();

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid " + exception.getName());
    }

    @ExceptionHandler(InvalidSyncRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidSyncRequest(
            InvalidSyncRequestException exception
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ProviderIntegrationException.class)
    public ResponseEntity<ApiErrorResponse> handleProviderFailure(
            ProviderIntegrationException exception
    ) {
        return buildResponse(HttpStatus.BAD_GATEWAY, "Soccer data provider request failed");
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), message));
    }
}
