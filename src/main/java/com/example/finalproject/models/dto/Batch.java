package com.example.finalproject.models.dto;


import lombok.Data;

import java.util.List;

@Data
public class Batch<T> {
    private List<T>data;
    private Long count;
}

