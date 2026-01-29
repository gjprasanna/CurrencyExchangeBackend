package com.example.demo.Service;
import com.example.demo.Dto.CurrencyDTO;
import com.example.demo.Dto.UploadSummary;

import java.util.List;

public interface CurrencyService {
    /**
     * Create a new currency
     */
    void createCurrency(CurrencyDTO dto);

    /**
     * Get all currencies with yesterday comparison
     */
    List<CurrencyDTO> getAll();

    /**
     * Get currency by code with yesterday comparison
     */
    CurrencyDTO getByCode(String code);

    /**
     * Process daily rate upload from bank
     */

    //jan21
//    void processDailyUpload(List<CurrencyDTO> dailyRates);

     UploadSummary processDailyUpload(List<CurrencyDTO> dailyRates);
}