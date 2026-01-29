package com.example.demo.Controller;
import com.example.demo.Dto.CurrencyPositionDTO;;
import com.example.demo.Service.CurrencyPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/positions")
//@CrossOrigin(origins = "*")
public class CurrencyPositionController {

    @Autowired
    private CurrencyPositionService positionService;

    // GET all positions
    @GetMapping("/list")
    public ResponseEntity<List<CurrencyPositionDTO>> getAllPositions() {
        List<CurrencyPositionDTO> positions = positionService.getAllPositions();
        return ResponseEntity.ok(positions);
    }

    // GET position by currency code
    @GetMapping("/{currencyCode}")
    public ResponseEntity<CurrencyPositionDTO> getPositionByCurrencyCode(@PathVariable String currencyCode) {
        try {
            CurrencyPositionDTO position = positionService.getPositionByCurrencyCode(currencyCode);
            return ResponseEntity.ok(position);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST create new position
    @PostMapping("/create")
    public ResponseEntity<CurrencyPositionDTO> createPosition(@RequestBody CurrencyPositionDTO positionDTO) {
        try {
            CurrencyPositionDTO created = positionService.createPosition(positionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT update position
    @PutMapping("/update/{currencyCode}")
    public ResponseEntity<CurrencyPositionDTO> updatePosition(
            @PathVariable String currencyCode,
            @RequestBody CurrencyPositionDTO positionDTO) {
        try {
            CurrencyPositionDTO updated = positionService.updatePosition(currencyCode, positionDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE position
    @DeleteMapping("/delete/{currencyCode}")
    public ResponseEntity<Void> deletePosition(@PathVariable String currencyCode) {
        try {
            positionService.deletePosition(currencyCode);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST create multiple positions at once
    @PostMapping("/create/batch")
    public ResponseEntity<List<CurrencyPositionDTO>> createPositionsBatch(@RequestBody List<CurrencyPositionDTO> positionDTOs) {
        try {
            List<CurrencyPositionDTO> created = positionDTOs.stream()
                    .map(dto -> positionService.createPosition(dto))
                    .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}