package com.example.demo.modal;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@Entity
@Table(name = "currency_positions")
public class CurrencyPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_code", nullable = false, unique = true, length = 3)
    private String currencyCode;  // USD, EUR, GBP, etc.

    @Column(name = "position_status", nullable = false, length = 10)
    private String positionStatus;  // LONG, SHORT, NEUTRAL

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;


    @Column(name = "amount", nullable = true, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    // Constructors
    public CurrencyPosition() {
        this.lastUpdated = LocalDateTime.now();
    }

    public CurrencyPosition(String currencyCode, String positionStatus) {
        this();
        this.currencyCode = currencyCode;
        this.positionStatus = positionStatus;
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

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}