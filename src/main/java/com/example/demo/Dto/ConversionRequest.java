package com.example.demo.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;


// ========== ConversionRequest.java ==========
/**
 * Request DTO for FX conversion
 */
public class ConversionRequest {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Buy code is required")
    @Size(min = 3, max = 3, message = "Buy code must be 3 characters")
    private String buyCode;

    @NotBlank(message = "Sell code is required")
    @Size(min = 3, max = 3, message = "Sell code must be 3 characters")
    private String sellCode;

    private BigDecimal customCustomerRate; // Optional override

    private LocalDate compareDate;//new field 27th jan

    // Constructors


    public ConversionRequest(BigDecimal amount, String buyCode, String sellCode, BigDecimal customCustomerRate) {
        this.amount = amount;
        this.buyCode = buyCode;
        this.sellCode = sellCode;
        this.customCustomerRate = customCustomerRate;
    }

    // Getters and Setters
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getBuyCode() { return buyCode; }
    public void setBuyCode(String buyCode) { this.buyCode = buyCode; }

    public String getSellCode() { return sellCode; }
    public void setSellCode(String sellCode) { this.sellCode = sellCode; }

    public BigDecimal getCustomCustomerRate() { return customCustomerRate; }
    public void setCustomCustomerRate(BigDecimal customCustomerRate) { this.customCustomerRate = customCustomerRate; }



}








//public record ConversionRequest(
//        @NotNull(message = "Amount is required")
//        @Positive(message = "Amount must be greater than zero")
//        BigDecimal amount,
//
//        @NotBlank(message = "Buy code is required")
//        @Size(min = 3, max = 3, message = "Buy code must be 3 characters")
//        String buyCode,
//
//        @NotBlank(message = "Sell code is required")
//        @Size(min = 3, max = 3, message = "Sell code must be 3 characters")
//        String sellCode,
//
//        BigDecimal customCustomerRate // Optional override
//) {}