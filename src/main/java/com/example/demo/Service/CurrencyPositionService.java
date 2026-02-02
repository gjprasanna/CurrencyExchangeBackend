package com.example.demo.Service;

import com.example.demo.Dto.CurrencyPositionDTO;
import java.util.List;

public interface CurrencyPositionService {

    // Get all currency positions
    List<CurrencyPositionDTO> getAllPositions();

    // Get position by currency code
    CurrencyPositionDTO getPositionByCurrencyCode(String currencyCode);

    // Create new position
    CurrencyPositionDTO createPosition(CurrencyPositionDTO positionDTO);

    //create or update
    CurrencyPositionDTO createOrUpdatePosition(CurrencyPositionDTO positionDTO);

    // Update existing position
    CurrencyPositionDTO updatePosition(String currencyCode, CurrencyPositionDTO positionDTO);

    // Delete position
    void deletePosition(String currencyCode);

    /**
     * Batch create positions (useful for bank API bulk uploads)
     */
    List<CurrencyPositionDTO> createPositionsBatch(List<CurrencyPositionDTO> positionDTOs);

    /**
     * Update position amount only (for frequent amount updates from bank)
     */
    CurrencyPositionDTO updatePositionAmount(String currencyCode, java.math.BigDecimal newAmount);

    // In CurrencyPositionService interface
    List<CurrencyPositionDTO> createOrUpdatePositionsBatch(List<CurrencyPositionDTO> positionDTOs);
}

