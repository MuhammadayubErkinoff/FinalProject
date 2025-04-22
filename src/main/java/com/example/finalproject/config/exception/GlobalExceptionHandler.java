package com.example.finalproject.config.exception;

import com.example.finalproject.models.dto.ErrorDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorDto> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {

        ErrorDto errorDto=new ErrorDto();
        errorDto.setTimestamp(LocalDateTime.now());
        errorDto.setStatus(ex.getStatusCode().value());
        errorDto.setError(ex.getReason());
        errorDto.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorDto, ex.getStatusCode());
    }

    @ExceptionHandler({SignatureException.class, MalformedJwtException.class})
    public ResponseEntity<ErrorDto> handleSignatureException(HttpServletRequest request) {

        ErrorDto errorDto=new ErrorDto();
        errorDto.setTimestamp(LocalDateTime.now());
        errorDto.setStatus(401);
        errorDto.setError("Invalid token");
        errorDto.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorDto> handleExpiredJwtException(HttpServletRequest request) {

        ErrorDto errorDto=new ErrorDto();
        errorDto.setTimestamp(LocalDateTime.now());
        errorDto.setStatus(401);
        errorDto.setError("Expired token");
        errorDto.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> exception(Exception ex, HttpServletRequest request) {

        ErrorDto errorDto=new ErrorDto();
        errorDto.setTimestamp(LocalDateTime.now());
        errorDto.setStatus(500);
        errorDto.setError("Internal Error");
        System.out.println(ex);
        errorDto.setPath(request.getRequestURI());

        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
