package com.example.chorvoqgisbackend.models.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorDto {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String path;
}
