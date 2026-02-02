//package com.example.demo.Controller;
//import com.example.demo.Dto.CurrencyPositionDTO;;
//import com.example.demo.Service.CurrencyPositionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/positions")
////@CrossOrigin(origins = "*")
//public class CurrencyPositionController {
//
//    @Autowired
//    private CurrencyPositionService positionService;
//
//    // GET all positions
//    @GetMapping("/list")
//    public ResponseEntity<List<CurrencyPositionDTO>> getAllPositions() {
//        List<CurrencyPositionDTO> positions = positionService.getAllPositions();
//        return ResponseEntity.ok(positions);
//    }
//
//    // GET position by currency code
//    @GetMapping("/{currencyCode}")
//    public ResponseEntity<CurrencyPositionDTO> getPositionByCurrencyCode(@PathVariable String currencyCode) {
//        try {
//            CurrencyPositionDTO position = positionService.getPositionByCurrencyCode(currencyCode);
//            return ResponseEntity.ok(position);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    // POST create new position
//    @PostMapping("/create")
//    public ResponseEntity<CurrencyPositionDTO> createPosition(@RequestBody CurrencyPositionDTO positionDTO) {
//        try {
//            CurrencyPositionDTO created = positionService.createPosition(positionDTO);
//            return ResponseEntity.status(HttpStatus.CREATED).body(created);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    // PUT update position
//    @PutMapping("/update/{currencyCode}")
//    public ResponseEntity<CurrencyPositionDTO> updatePosition(
//            @PathVariable String currencyCode,
//            @RequestBody CurrencyPositionDTO positionDTO) {
//        try {
//            CurrencyPositionDTO updated = positionService.updatePosition(currencyCode, positionDTO);
//            return ResponseEntity.ok(updated);
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    // DELETE position
//    @DeleteMapping("/delete/{currencyCode}")
//    public ResponseEntity<Void> deletePosition(@PathVariable String currencyCode) {
//        try {
//            positionService.deletePosition(currencyCode);
//            return ResponseEntity.noContent().build();
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    // POST create multiple positions at once
//    @PostMapping("/create/batch")
//    public ResponseEntity<List<CurrencyPositionDTO>> createPositionsBatch(@RequestBody List<CurrencyPositionDTO> positionDTOs) {
//        try {
//            List<CurrencyPositionDTO> created = positionDTOs.stream()
//                    .map(dto -> positionService.createPosition(dto))
//                    .collect(Collectors.toList());
//            return ResponseEntity.status(HttpStatus.CREATED).body(created);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//}
package com.example.demo.Controller;

import com.example.demo.Dto.CurrencyPositionDTO;
import com.example.demo.Service.CurrencyPositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Currency Position operations
 * Handles CRUD operations with BNR rates and amounts
 */
@RestController
@RequestMapping("/api/positions")
public class CurrencyPositionController {

    private static final Logger log = LoggerFactory.getLogger(CurrencyPositionController.class);
    private final CurrencyPositionService positionService;

    public CurrencyPositionController(CurrencyPositionService positionService) {
        this.positionService = positionService;
    }

    /**
     * GET all positions with BNR rates
     */
    @GetMapping("/list")
    public ResponseEntity<List<CurrencyPositionDTO>> getAllPositions() {
        log.debug("Fetching all positions");
        List<CurrencyPositionDTO> positions = positionService.getAllPositions();
        return ResponseEntity.ok(positions);
    }

    /**
     * GET position by currency code
     */
    @GetMapping("/{currencyCode}")
    public ResponseEntity<?> getPositionByCurrencyCode(@PathVariable String currencyCode) {
        log.info("Fetching position for: {}", currencyCode);

        CurrencyPositionDTO position = positionService.getPositionByCurrencyCode(currencyCode);
        return ResponseEntity.ok(position);
    }

    /**
     * POST create new position
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPosition(@RequestBody CurrencyPositionDTO positionDTO) {
        log.info("Creating position: {}", positionDTO.getCurrencyCode());

        CurrencyPositionDTO created = positionService.createPosition(positionDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Position created successfully");
        response.put("data", created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PUT update position (status and/or amount)
     */
    @PutMapping("/update/{currencyCode}")
    public ResponseEntity<?> updatePosition(
            @PathVariable String currencyCode,
            @RequestBody CurrencyPositionDTO positionDTO) {

        log.info("Updating position: {}", currencyCode);

        CurrencyPositionDTO updated = positionService.updatePosition(currencyCode, positionDTO);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Position updated successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/upsert")
    public ResponseEntity<CurrencyPositionDTO> createOrUpdatePosition(@RequestBody CurrencyPositionDTO positionDTO) {
        log.info("Upserting position for currency: {}", positionDTO.getCurrencyCode());
        CurrencyPositionDTO savedPosition = positionService.createOrUpdatePosition(positionDTO);
        return ResponseEntity.ok(savedPosition);
    }

    /**
     * PATCH update position amount only (for frequent bank API updates)
     */
    @PatchMapping("/update-amount/{currencyCode}")
    public ResponseEntity<?> updatePositionAmount(
            @PathVariable String currencyCode,
            @RequestBody Map<String, BigDecimal> payload) {

        log.info("Updating amount for: {}", currencyCode);

        BigDecimal newAmount = payload.get("amount");
        if (newAmount == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Amount is required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        CurrencyPositionDTO updated = positionService.updatePositionAmount(currencyCode, newAmount);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Amount updated successfully");
        response.put("data", updated);

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE position
     */
    @DeleteMapping("/delete/{currencyCode}")
    public ResponseEntity<?> deletePosition(@PathVariable String currencyCode) {
        log.info("Deleting position: {}", currencyCode);

        positionService.deletePosition(currencyCode);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Position deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * POST batch create positions (useful for bank API bulk uploads)
     */
    @PostMapping("/create/batch")
    public ResponseEntity<?> createPositionsBatch(@RequestBody List<CurrencyPositionDTO> positionDTOs) {
        log.info("Batch creating {} positions", positionDTOs.size());

        List<CurrencyPositionDTO> created = positionService.createPositionsBatch(positionDTOs);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", created.size() + " positions created successfully");
        response.put("totalRequested", positionDTOs.size());
        response.put("successCount", created.size());
        response.put("failedCount", positionDTOs.size() - created.size());
        response.put("data", created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/batch/upsert")
    public ResponseEntity<List<CurrencyPositionDTO>> batchUpsertPositions(
            @RequestBody List<CurrencyPositionDTO> positionDTOs) {
        log.info("Batch upserting {} positions", positionDTOs.size());

        // ✅ CORRECT: Calls proper batch upsert
        List<CurrencyPositionDTO> savedPositions =
                positionService.createOrUpdatePositionsBatch(positionDTOs);

        return ResponseEntity.ok(savedPositions);
    }

}