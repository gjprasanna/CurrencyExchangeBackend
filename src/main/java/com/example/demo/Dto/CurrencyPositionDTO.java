package com.example.demo.Dto;

import java.time.LocalDateTime;

public class CurrencyPositionDTO {

    private Long id;
    private String currencyCode;
    private String positionStatus;
    private LocalDateTime lastUpdated;

    // Constructors
    public CurrencyPositionDTO() {
    }

    public CurrencyPositionDTO(Long id, String currencyCode, String positionStatus, LocalDateTime lastUpdated) {
        this.id = id;
        this.currencyCode = currencyCode;
        this.positionStatus = positionStatus;
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

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}