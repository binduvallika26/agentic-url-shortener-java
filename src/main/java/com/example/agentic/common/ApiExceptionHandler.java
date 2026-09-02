package com.example.agentic.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(DomainException.class)
    ProblemDetail domain(DomainException ex, HttpServletRequest request) {
        var detail=ProblemDetail.forStatusAndDetail(ex.status(), ex.getMessage());
        detail.setType(URI.create("urn:problem:"+ex.code())); detail.setTitle(ex.code());
        detail.setProperty("correlationId", request.getAttribute(CorrelationFilter.CORRELATION_ID)); return detail;
    }
}
