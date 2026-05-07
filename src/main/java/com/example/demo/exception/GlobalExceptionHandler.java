package com.example.demo.exception;

import com.example.demo.dto.AnalysisResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AnalysisResponse> handleBadRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                new AnalysisResponse("Bad Request", ex.getMessage(),
                        "Fix the request and retry.", 0.0)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AnalysisResponse> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new AnalysisResponse("Internal Server Error",
                        "An unexpected error occurred: " + ex.getMessage(),
                        "Check server logs for details.", 0.0)
        );
    }
}