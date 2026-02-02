//package com.example.demo.Service.Impl;
//
//import com.example.demo.Dto.CurrencyPositionDTO;
//import com.example.demo.modal.CurrencyPosition;
//import com.example.demo.Repository.CurrencyPositionRepository;
//import com.example.demo.Service.CurrencyPositionService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class CurrencyPositionServiceImpl implements CurrencyPositionService {
//
//    @Autowired
//    private CurrencyPositionRepository positionRepository;
//
//    @Override
//    public List<CurrencyPositionDTO> getAllPositions() {
//        return positionRepository.findAll()
//                .stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public CurrencyPositionDTO getPositionByCurrencyCode(String currencyCode) {
//        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
//                .orElseThrow(() -> new RuntimeException("Position not found for currency: " + currencyCode));
//        return convertToDTO(position);
//    }
//
//    @Override
//    public CurrencyPositionDTO createPosition(CurrencyPositionDTO positionDTO) {
//        // Check if position already exists
//        if (positionRepository.existsByCurrencyCode(positionDTO.getCurrencyCode())) {
//            throw new RuntimeException("Position already exists for currency: " + positionDTO.getCurrencyCode());
//        }
//
//        CurrencyPosition position = new CurrencyPosition();
//        position.setCurrencyCode(positionDTO.getCurrencyCode());
//        position.setPositionStatus(positionDTO.getPositionStatus());
//        position.setLastUpdated(LocalDateTime.now());
//
//        CurrencyPosition savedPosition = positionRepository.save(position);
//        return convertToDTO(savedPosition);
//    }
//
//    @Override
//    public CurrencyPositionDTO updatePosition(String currencyCode, CurrencyPositionDTO positionDTO) {
//        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
//                .orElseThrow(() -> new RuntimeException("Position not found for currency: " + currencyCode));
//
//        position.setPositionStatus(positionDTO.getPositionStatus());
//        position.setLastUpdated(LocalDateTime.now());
//
//        CurrencyPosition updatedPosition = positionRepository.save(position);
//        return convertToDTO(updatedPosition);
//    }
//
//    @Override
//    public void deletePosition(String currencyCode) {
//        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
//                .orElseThrow(() -> new RuntimeException("Position not found for currency: " + currencyCode));
//        positionRepository.delete(position);
//    }
//
//    // Helper method to convert Entity to DTO
//    private CurrencyPositionDTO convertToDTO(CurrencyPosition position) {
//        return new CurrencyPositionDTO(
//                position.getId(),
//                position.getCurrencyCode(),
//                position.getPositionStatus(),
//                position.getLastUpdated()
//        );
//    }
//}

package com.example.demo.Service.Impl;

import com.example.demo.Dto.CurrencyPositionDTO;
import com.example.demo.modal.Currency;
import com.example.demo.modal.CurrencyPosition;
import com.example.demo.Repository.CurrencyPositionRepository;
import com.example.demo.Repository.CurrencyRepository;
import com.example.demo.Service.CurrencyPositionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CurrencyPositionService
 * Fetches BNR rate from Currency table for each position
 */
@Service
@Transactional
public class CurrencyPositionServiceImpl implements CurrencyPositionService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyPositionServiceImpl.class);

    private final CurrencyPositionRepository positionRepository;
    private final CurrencyRepository currencyRepository;

    public CurrencyPositionServiceImpl(CurrencyPositionRepository positionRepository,
                                       CurrencyRepository currencyRepository) {
        this.positionRepository = positionRepository;
        this.currencyRepository = currencyRepository;
    }

    @Override
    public List<CurrencyPositionDTO> getAllPositions() {
        log.debug("Fetching all currency positions");
        return positionRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CurrencyPositionDTO getPositionByCurrencyCode(String currencyCode) {
        log.info("Fetching position for currency: {}", currencyCode);

        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Position not found for currency: " + currencyCode));

        return convertToDTO(position);
    }

    @Override
    public CurrencyPositionDTO createPosition(CurrencyPositionDTO positionDTO) {
        log.info("Creating position for currency: {}", positionDTO.getCurrencyCode());

        // Validate currency code
        validateCurrencyCode(positionDTO.getCurrencyCode());

        // Check if position already exists
        if (positionRepository.existsByCurrencyCode(positionDTO.getCurrencyCode())) {
            throw new IllegalArgumentException(
                    "Position already exists for currency: " + positionDTO.getCurrencyCode());
        }

        // Validate position status
        validatePositionStatus(positionDTO.getPositionStatus());

        CurrencyPosition position = new CurrencyPosition();
        position.setCurrencyCode(positionDTO.getCurrencyCode().toUpperCase());
        position.setPositionStatus(positionDTO.getPositionStatus().toUpperCase());
        position.setAmount(positionDTO.getAmount() != null ? positionDTO.getAmount() : BigDecimal.ZERO);
        position.setLastUpdated(LocalDateTime.now());

        CurrencyPosition savedPosition = positionRepository.save(position);
        log.info("Position created successfully: {}", savedPosition.getCurrencyCode());

        return convertToDTO(savedPosition);
    }

    @Override
    public CurrencyPositionDTO updatePosition(String currencyCode, CurrencyPositionDTO positionDTO) {
        log.info("Updating position for currency: {}", currencyCode);

        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Position not found for currency: " + currencyCode));

        // Update position status if provided
        if (positionDTO.getPositionStatus() != null) {
            validatePositionStatus(positionDTO.getPositionStatus());
            position.setPositionStatus(positionDTO.getPositionStatus().toUpperCase());
        }

        // Update amount if provided
        if (positionDTO.getAmount() != null) {
            position.setAmount(positionDTO.getAmount());
        }

        position.setLastUpdated(LocalDateTime.now());

        CurrencyPosition updatedPosition = positionRepository.save(position);
        log.info("Position updated successfully: {}", updatedPosition.getCurrencyCode());

        return convertToDTO(updatedPosition);
    }



    /**
     * Create or update position (UPSERT operation)
     * This is ideal for daily position updates
     */
    @Override
    public CurrencyPositionDTO createOrUpdatePosition(CurrencyPositionDTO positionDTO) {
        log.info("Creating or updating position for currency: {}", positionDTO.getCurrencyCode());

        // Validate inputs
        validateCurrencyCode(positionDTO.getCurrencyCode());
        validatePositionStatus(positionDTO.getPositionStatus());

        String currencyCode = positionDTO.getCurrencyCode().toUpperCase();

        // Check if position exists
        Optional<CurrencyPosition> existingPosition = positionRepository.findByCurrencyCode(currencyCode);

        CurrencyPosition position;
        if (existingPosition.isPresent()) {
            // Update existing position
            log.info("Position exists, updating for currency: {}", currencyCode);
            position = existingPosition.get();
            position.setPositionStatus(positionDTO.getPositionStatus().toUpperCase());
            position.setAmount(positionDTO.getAmount() != null ? positionDTO.getAmount() : BigDecimal.ZERO);
            position.setLastUpdated(LocalDateTime.now());
        } else {
            // Create new position
            log.info("Position does not exist, creating for currency: {}", currencyCode);
            position = new CurrencyPosition();
            position.setCurrencyCode(currencyCode);
            position.setPositionStatus(positionDTO.getPositionStatus().toUpperCase());
            position.setAmount(positionDTO.getAmount() != null ? positionDTO.getAmount() : BigDecimal.ZERO);
            position.setLastUpdated(LocalDateTime.now());
        }

        CurrencyPosition savedPosition = positionRepository.save(position);
        log.info("Position saved successfully: {}", savedPosition.getCurrencyCode());

        return convertToDTO(savedPosition);
    }




    @Override
    public void deletePosition(String currencyCode) {
        log.info("Deleting position for currency: {}", currencyCode);

        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Position not found for currency: " + currencyCode));

        positionRepository.delete(position);
        log.info("Position deleted successfully: {}", currencyCode);
    }

    @Override
    public List<CurrencyPositionDTO> createPositionsBatch(List<CurrencyPositionDTO> positionDTOs) {
        log.info("Batch creating {} positions", positionDTOs.size());

        return positionDTOs.stream()
                .map(dto -> {
                    try {
                        return createPosition(dto);
                    } catch (IllegalArgumentException e) {
                        log.warn("Failed to create position for {}: {}",
                                dto.getCurrencyCode(), e.getMessage());
                        // Skip duplicates or invalid positions
                        return null;
                    }
                })
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    @Override
    public CurrencyPositionDTO updatePositionAmount(String currencyCode, BigDecimal newAmount) {
        log.info("Updating amount for currency: {} to {}", currencyCode, newAmount);

        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Position not found for currency: " + currencyCode));

        position.setAmount(newAmount);
        position.setLastUpdated(LocalDateTime.now());

        CurrencyPosition updatedPosition = positionRepository.save(position);
        return convertToDTO(updatedPosition);
    }

    /**
     * Convert Entity to DTO and fetch BNR rate from Currency table
     */
    private CurrencyPositionDTO convertToDTO(CurrencyPosition position) {
        CurrencyPositionDTO dto = new CurrencyPositionDTO();
        dto.setId(position.getId());
        dto.setCurrencyCode(position.getCurrencyCode());
        dto.setPositionStatus(position.getPositionStatus());
        dto.setAmount(position.getAmount());
        dto.setLastUpdated(position.getLastUpdated());

        // Fetch BNR rate from Currency table
        BigDecimal bnrRate = fetchBnrRate(position.getCurrencyCode());
        dto.setBnrRate(bnrRate);

        return dto;
    }

    /**
     * Fetch BNR rate from Currency table (latest rate for the currency)
     */
    private BigDecimal fetchBnrRate(String currencyCode) {
        try {
            // Get latest currency rate
            Optional<Currency> currencyOpt = currencyRepository.findLatestByCode(currencyCode);

            if (currencyOpt.isEmpty()) {
                log.warn("No currency rate found for: {}", currencyCode);
                return null;
            }

            Currency currency = currencyOpt.get();
            return currency.getBnrrate();

        } catch (Exception e) {
            log.error("Error fetching BNR rate for {}: {}", currencyCode, e.getMessage());
            return null;
        }
    }

    /**
     * Validate currency code format
     */
    private void validateCurrencyCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be empty");
        }
        if (code.length() != 3) {
            throw new IllegalArgumentException("Currency code must be 3 characters");
        }
    }

    /**
     * Validate position status
     */
    private void validatePositionStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Position status cannot be empty");
        }

        String upperStatus = status.toUpperCase();
        if (!upperStatus.equals("LONG") && !upperStatus.equals("SHORT") && !upperStatus.equals("NEUTRAL")) {
            throw new IllegalArgumentException(
                    "Position status must be LONG, SHORT, or NEUTRAL");
        }
    }



    // In CurrencyPositionServiceImpl
    @Override
    public List<CurrencyPositionDTO> createOrUpdatePositionsBatch(List<CurrencyPositionDTO> positionDTOs) {
        log.info("Batch upserting {} positions", positionDTOs.size());

        return positionDTOs.stream()
                .map(this::createOrUpdatePosition)  // ✅ Uses upsert logic
                .collect(Collectors.toList());
    }
}