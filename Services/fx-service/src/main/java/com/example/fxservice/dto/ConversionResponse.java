package com.example.fxservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    private String from;
    private String to;
    private double amount;
    private double converted;
    private double rate;
}
