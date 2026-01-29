package com.example.demo.Service.Impl;

import com.example.demo.Dto.CurrencyPositionDTO;
import com.example.demo.modal.CurrencyPosition;
import com.example.demo.Repository.CurrencyPositionRepository;
import com.example.demo.Service.CurrencyPositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CurrencyPositionServiceImpl implements CurrencyPositionService {

    @Autowired
    private CurrencyPositionRepository positionRepository;

    @Override
    public List<CurrencyPositionDTO> getAllPositions() {
        return positionRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CurrencyPositionDTO getPositionByCurrencyCode(String currencyCode) {
        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
                .orElseThrow(() -> new RuntimeException("Position not found for currency: " + currencyCode));
        return convertToDTO(position);
    }

    @Override
    public CurrencyPositionDTO createPosition(CurrencyPositionDTO positionDTO) {
        // Check if position already exists
        if (positionRepository.existsByCurrencyCode(positionDTO.getCurrencyCode())) {
            throw new RuntimeException("Position already exists for currency: " + positionDTO.getCurrencyCode());
        }

        CurrencyPosition position = new CurrencyPosition();
        position.setCurrencyCode(positionDTO.getCurrencyCode());
        position.setPositionStatus(positionDTO.getPositionStatus());
        position.setLastUpdated(LocalDateTime.now());

        CurrencyPosition savedPosition = positionRepository.save(position);
        return convertToDTO(savedPosition);
    }

    @Override
    public CurrencyPositionDTO updatePosition(String currencyCode, CurrencyPositionDTO positionDTO) {
        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
                .orElseThrow(() -> new RuntimeException("Position not found for currency: " + currencyCode));

        position.setPositionStatus(positionDTO.getPositionStatus());
        position.setLastUpdated(LocalDateTime.now());

        CurrencyPosition updatedPosition = positionRepository.save(position);
        return convertToDTO(updatedPosition);
    }

    @Override
    public void deletePosition(String currencyCode) {
        CurrencyPosition position = positionRepository.findByCurrencyCode(currencyCode)
                .orElseThrow(() -> new RuntimeException("Position not found for currency: " + currencyCode));
        positionRepository.delete(position);
    }

    // Helper method to convert Entity to DTO
    private CurrencyPositionDTO convertToDTO(CurrencyPosition position) {
        return new CurrencyPositionDTO(
                position.getId(),
                position.getCurrencyCode(),
                position.getPositionStatus(),
                position.getLastUpdated()
        );
    }
}