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

    // Update existing position
    CurrencyPositionDTO updatePosition(String currencyCode, CurrencyPositionDTO positionDTO);

    // Delete position
    void deletePosition(String currencyCode);
}