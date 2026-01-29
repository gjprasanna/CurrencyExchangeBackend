package com.example.demo.Service;
import com.example.demo.Dto.ConversionRequest;
import com.example.demo.Dto.ConversionResponse;

public interface FXService {
    /**
     * Convert currency and calculate rates
     */
    ConversionResponse convertCurrency(ConversionRequest request);
}