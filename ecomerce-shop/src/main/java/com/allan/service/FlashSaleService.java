package com.allan.service;

import com.allan.dto.FlashSaleDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface FlashSaleService {

    List<FlashSaleDTO> getAllFlashSales() throws Exception;

    List<FlashSaleDTO> getCurrentlyLiveFlashSales() throws Exception;

    FlashSaleDTO createFlashSale(String title, Integer discountPercent,
                                 LocalDateTime startTime, LocalDateTime endTime,
                                 List<Long> productIds) throws Exception;

    FlashSaleDTO addProductsToFlashSale(Long flashSaleId, List<Long> productIds) throws Exception;

    void deactivateFlashSale(Long flashSaleId) throws Exception;
}