//package com.example.demo.Dto;
//
//
//import java.math.BigDecimal;
//
//public record CurrencyDTO(
//        String code,
//        String name,
//        BigDecimal bnrrate,
//        BigDecimal buyrate,
//        BigDecimal sellrate,
//        BigDecimal buyspreadrate,
//        BigDecimal sellspreadrate,
//        BigDecimal buyCustomerRate,
//        BigDecimal sellCustomerRate
//
//) {}

package com.example.demo.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// ========== CurrencyDTO.java ==========
/**
 * DTO for displaying currency with yesterday comparison
 */
public record CurrencyDTO(
        String code,
        String name,

        // TODAY'S RATES
        BigDecimal bnrrate,
        BigDecimal buyrate,
        BigDecimal sellrate,
        BigDecimal buyspreadrate,
        BigDecimal sellspreadrate,
        BigDecimal buyCustomerRate,    // buyrate + buyspreadrate
        BigDecimal sellCustomerRate,   // sellrate + sellspreadrate

        // YESTERDAY COMPARISON - BNR RATE
        BigDecimal previousBnrRate,
        BigDecimal bnrRateDifference,      // Today - Yesterday
        BigDecimal bnrRateChangePercent,   // Percentage change

        // YESTERDAY COMPARISON - BUY T24 RATE
        BigDecimal previousBuyCustomerRate,
        BigDecimal buyRateDifference,
        BigDecimal buyRateChangePercent,

        // YESTERDAY COMPARISON - SELL T24 RATE
        BigDecimal previousSellCustomerRate,
        BigDecimal sellRateDifference,
        BigDecimal sellRateChangePercent,

        Boolean hasHistoricalData
) {}