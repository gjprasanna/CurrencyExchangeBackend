//package com.example.demo.Controller;
//
//import com.example.demo.Dto.CurrencyDTO;
//import com.example.demo.Service.CurrencyService;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/currencies")
//public class CurrencyController {
//
//    private final CurrencyService service;
//
//    public CurrencyController(CurrencyService service) {
//        this.service = service;
//    }
//
//    @PostMapping("/add")
//    public ResponseEntity<String> add(@RequestBody CurrencyDTO dto) {
//        service.createCurrency(dto); // If this throws, GlobalExceptionHandler catches it
//        return ResponseEntity.status(HttpStatus.CREATED).body("Currency Saved Successfully!");
//    }
//
//    @GetMapping("/list")
//    public ResponseEntity<List<CurrencyDTO>> listAll() {
//        return ResponseEntity.ok(service.getAll());
//    }
//
//    @GetMapping("/{code}")
//    public ResponseEntity<?> getByCode(@PathVariable String code) {
//        try {
//            CurrencyDTO currency = service.getByCode(code);
//            return ResponseEntity.ok(currency);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
//    }
//}


//Jan21
//package com.example.demo.Controller;
//
//import com.example.demo.Dto.CurrencyDTO;
//import com.example.demo.Service.CurrencyService;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/currencies")
//public class CurrencyController {
//
//    private final CurrencyService service;
//
//    public CurrencyController(CurrencyService service) {
//        this.service = service;
//    }
//
//    /**
//     * Add a single currency (for testing)
//     */
//    @PostMapping("/add")
//    public ResponseEntity<String> add(@RequestBody CurrencyDTO dto) {
//        service.createCurrency(dto);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body("Currency Saved Successfully!");
//    }
//
//    /**
//     * Get all currencies with yesterday comparison
//     */
//    @GetMapping("/list")
//    public ResponseEntity<List<CurrencyDTO>> listAll() {
//        return ResponseEntity.ok(service.getAll());
//    }
//
//    /**
//     * Get specific currency by code
//     */
//    @GetMapping("/{code}")
//    public ResponseEntity<?> getByCode(@PathVariable String code) {
//        try {
//            CurrencyDTO currency = service.getByCode(code);
//            return ResponseEntity.ok(currency);
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(e.getMessage());
//        }
//    }
//
//    /**
//     * Daily upload endpoint (for bank's JSON upload)
//     */
////    @PostMapping("/daily-upload")
////    public ResponseEntity<String> dailyUpload(@RequestBody List<CurrencyDTO> dailyRates) {
////        try {
////            service.processDailyUpload(dailyRates);
////            return ResponseEntity.ok("Daily rates uploaded successfully for " + dailyRates.size() + " currencies");
////        } catch (Exception e) {
////            e.printStackTrace(); // ✅ This will show in console
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
////                    .body("Failed to upload daily rates: " + e.getMessage());
////        }
////    }
//
//    @PostMapping("/daily-upload")
//    public ResponseEntity<String> dailyUpload(@RequestBody List<CurrencyDTO> dailyRates) {
//        System.out.println("=== DAILY UPLOAD STARTED ===");
//        System.out.println("Received " + (dailyRates != null ? dailyRates.size() : 0) + " currencies for daily upload");
//
//        if (dailyRates == null || dailyRates.isEmpty()) {
//            System.out.println("Empty or null daily rates received");
//            return ResponseEntity.badRequest().body("No currency data provided");
//        }
//
//        // Log each currency received
//        for (CurrencyDTO dto : dailyRates) {
//            System.out.println("Processing: " + dto.code() + " - " + dto.name() + " (BNR: " + dto.bnrrate() + ")");
//        }
//
//        try {
//            service.processDailyUpload(dailyRates);
//            System.out.println("Daily upload completed successfully");
//            return ResponseEntity.ok("Daily rates uploaded successfully for " + dailyRates.size() + " currencies");
//
//        } catch (Exception e) {
//            System.err.println("=== DAILY UPLOAD FAILED ===");
//            System.err.println("Error type: " + e.getClass().getName());
//            System.err.println("Error message: " + e.getMessage());
//            System.err.println("Full stack trace: ");
//            e.printStackTrace();
//
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to upload daily rates: " + e.getMessage());
//        }
//    }
//}

package com.example.demo.Controller;

import com.example.demo.Dto.CurrencyDTO;
import com.example.demo.Dto.UploadSummary;
import com.example.demo.Service.CurrencyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private static final Logger log = LoggerFactory.getLogger(CurrencyController.class);
    private final CurrencyService service;

    public CurrencyController(CurrencyService service) {
        this.service = service;
    }

    /**
     * Add a single currency (for testing)
     */
    @PostMapping("/add")
    public ResponseEntity<?> add(@Valid @RequestBody CurrencyDTO dto) {
        log.info("Adding currency: {}", dto.code());

        service.createCurrency(dto);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Currency saved successfully");
        response.put("code", dto.code());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all currencies with yesterday comparison
     */
    @GetMapping("/list")
    public ResponseEntity<List<CurrencyDTO>> listAll() {
        log.debug("Fetching all currencies");
        return ResponseEntity.ok(service.getAll());
    }

    /**
     * Get specific currency by code
     */
    @GetMapping("/{code}")
    public ResponseEntity<?> getByCode(@PathVariable String code) {
        log.info("Fetching currency: {}", code);

        CurrencyDTO currency = service.getByCode(code);
        return ResponseEntity.ok(currency);
    }

    /**
     * Daily upload endpoint (for bank's JSON upload)
     * Returns detailed summary of successes and failures
     */
    @PostMapping("/daily-upload")
    public ResponseEntity<?> dailyUpload(@RequestBody List<CurrencyDTO> dailyRates) {
        log.info("=== DAILY UPLOAD STARTED ===");
        log.info("Received {} currencies for daily upload", dailyRates != null ? dailyRates.size() : 0);

        // Validate input
        if (dailyRates == null || dailyRates.isEmpty()) {
            log.warn("Empty or null daily rates received");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "No currency data provided");

            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Log sample of received data
        if (log.isDebugEnabled()) {
            dailyRates.stream().limit(3).forEach(dto ->
                    log.debug("Sample: {} - {} (BNR: {})", dto.code(), dto.name(), dto.bnrrate())
            );
        }

        // Process upload
        UploadSummary summary = service.processDailyUpload(dailyRates);

        log.info("Daily upload completed - Success: {}, Failed: {}",
                summary.successCount(), summary.failureCount());

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("success", summary.isFullSuccess());
        response.put("totalProcessed", summary.totalProcessed());
        response.put("successCount", summary.successCount());
        response.put("failureCount", summary.failureCount());
        response.put("succeeded", summary.succeeded());

        if (summary.hasFailures()) {
            response.put("failed", summary.failed());
            log.warn("Upload had {} failures", summary.failureCount());
        }

        // Determine HTTP status
        HttpStatus status;
        if (summary.isFullSuccess()) {
            status = HttpStatus.OK;
            response.put("message", "All currencies uploaded successfully");
        } else if (summary.successCount() > 0) {
            status = HttpStatus.MULTI_STATUS; // 207 - Partial success
            response.put("message", "Partial upload completed with some failures");
        } else {
            status = HttpStatus.BAD_REQUEST;
            response.put("message", "All currencies failed to upload");
        }

        log.info("=== DAILY UPLOAD FINISHED ===");

        return ResponseEntity.status(status).body(response);
    }
}