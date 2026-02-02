package com.example.demo.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Currency Position with T24 rate and amount
 * T24 rate is fetched from Currency table based on position status
 */
public class CurrencyPositionDTO {

    private Long id;
    private String currencyCode;
    private String positionStatus; // LONG, SHORT, NEUTRAL

    /**
     * BNR Rate from Currency table
     * Fetched from the latest currency record for this currency code
     */
    private BigDecimal bnrRate;

    /**
     * Position amount from bank API
     */
    private BigDecimal amount;

    private LocalDateTime lastUpdated;

    // Constructors
    public CurrencyPositionDTO() {}

    public CurrencyPositionDTO(Long id, String currencyCode, String positionStatus,
                               BigDecimal bnrRate, BigDecimal amount, LocalDateTime lastUpdated) {
        this.id = id;
        this.currencyCode = currencyCode;
        this.positionStatus = positionStatus;
        this.bnrRate = bnrRate;
        this.amount = amount;
        this.lastUpdated = lastUpdated;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getPositionStatus() {
        return positionStatus;
    }

    public void setPositionStatus(String positionStatus) {
        this.positionStatus = positionStatus;
    }

    public BigDecimal getBnrRate() {
        return bnrRate;
    }

    public void setBnrRate(BigDecimal bnrRate) {
        this.bnrRate = bnrRate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}