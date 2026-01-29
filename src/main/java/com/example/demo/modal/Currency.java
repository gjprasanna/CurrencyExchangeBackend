////package com.example.demo.Modal;
////
////import jakarta.persistence.*;
////import lombok.AllArgsConstructor;
////import lombok.Data;
////import lombok.NoArgsConstructor;
////import java.math.BigDecimal;
////
////@Entity
////@Table(name = "currencies")
////@Data
////@NoArgsConstructor
////@AllArgsConstructor
////public class Currency {
////
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////
////    @Column(nullable = false, unique = true, length = 3)
////    private String code;
////
////    @Column(nullable = false)
////    private String name;
////
////    @Column(nullable = false, precision = 19, scale = 6)
////    private BigDecimal bnrrate;
////
////    @Column(nullable = false, precision = 19, scale = 6)
////    private BigDecimal buyrate;
////
////    @Column(nullable = false, precision = 19, scale = 6)
////    private BigDecimal sellrate;
////
////    @Column(nullable = false, precision = 19, scale = 6)
////    private BigDecimal buyspreadrate;
////
////    @Column(nullable = false, precision = 19, scale = 6)
////    private BigDecimal sellspreadrate;
////
////
////}
//
//package com.example.demo.modal;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
///**
// * Entity representing currency rates and their historical comparison data.
// */
//@Entity
//@Table(name = "currencies")
//@Getter // Recommended over @Data for JPA entities to avoid heavy equals/hashCode on proxy objects
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class Currency {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true, length = 3)
//    private String code;
//
//    @Column(nullable = false, length = 100)
//    private String name;
//
//    @Column(nullable = false)
//    private LocalDate rateDate;
//
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime uploadedAt;
//
//    @Column
//    private LocalDateTime lastModified;
//
//    // TODAY'S RATES
//    // Initializing with ZERO prevents null checks in the Service layer math logic
//    @Column(nullable = false, precision = 19, scale = 6)
//    private BigDecimal bnrrate = BigDecimal.ZERO;
//
//    @Column(nullable = false, precision = 19, scale = 6)
//    private BigDecimal buyrate = BigDecimal.ZERO;
//
//    @Column(nullable = false, precision = 19, scale = 6)
//    private BigDecimal sellrate = BigDecimal.ZERO;
//
//    @Column(nullable = false, precision = 19, scale = 6)
//    private BigDecimal buyspreadrate = BigDecimal.ZERO;
//
//    @Column(nullable = false, precision = 19, scale = 6)
//    private BigDecimal sellspreadrate = BigDecimal.ZERO;
//
//    // YESTERDAY'S RATES
//    // Explicit precision/scale ensures comparison percentages remain accurate
//    @Column(precision = 19, scale = 6)
//    private BigDecimal previousBnrRate;
//
//    @Column(precision = 19, scale = 6)
//    private BigDecimal previousBuyrate;
//
//    @Column(precision = 19, scale = 6)
//    private BigDecimal previousSellrate;
//
//    @Column(precision = 19, scale = 6)
//    private BigDecimal previousBuyspreadrate;
//
//    @Column(precision = 19, scale = 6)
//    private BigDecimal previousSellspreadrate;
//
//    // Historical T24 rates for UI dashboard comparison
//    @Column(name = "previous_buy_t24_rate", precision = 19, scale = 6)
//    private BigDecimal previousBuyT24Rate;
//
//    @Column(name = "previous_sell_t24_rate", precision = 19, scale = 6)
//    private BigDecimal previousSellT24Rate;
//
//    // OPTIMISTIC LOCKING
//    // Prevents two users/processes from updating the same rate at the same time
//    @Version
//    @Column(nullable = false) // Add this to enforce database integrity
//    private Integer version = 0; // Initialize with 0
//
//    /**
//     * Simple state check for the Service layer to decide if comparison logic should run.
//     */
//    public boolean hasHistoricalData() {
//        return previousBnrRate != null;
//    }
//
//    @PrePersist
//    protected void onCreate() {
//        uploadedAt = LocalDateTime.now();
//        lastModified = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        lastModified = LocalDateTime.now();
//    }
//}

package com.example.demo.modal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Time-series entity - stores one record per currency per day
 * Keeps 30 days in this table, older data moved to CurrencyHistory
 */
@Entity
@Table(name = "currency_rates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"code", "rate_date"}),
        indexes = {
                @Index(name = "idx_code_date", columnList = "code, rate_date DESC"),
                @Index(name = "idx_rate_date", columnList = "rate_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3)
    private String code; // USD, EUR, etc. - NOT unique (multiple dates per code)

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "rate_date", nullable = false)
    private LocalDate rateDate; // The date this rate is for

    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column
    private LocalDateTime lastModified;

    // DAILY RATES
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal bnrrate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal buyrate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal sellrate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal buyspreadrate = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal sellspreadrate = BigDecimal.ZERO;

    // OPTIMISTIC LOCKING
    @Version
    @Column(nullable = false)
    private Integer version = 0;

    // Helper method to calculate T24 rates
    public BigDecimal getBuyT24Rate() {
        return buyrate.add(buyspreadrate);
    }

    public BigDecimal getSellT24Rate() {
        return sellrate.add(sellspreadrate);
    }

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        lastModified = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModified = LocalDateTime.now();
    }
}