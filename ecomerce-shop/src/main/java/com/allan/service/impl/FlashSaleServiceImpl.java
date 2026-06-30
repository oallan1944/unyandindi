package com.allan.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.allan.dto.FlashSaleDTO;
import com.allan.mapper.FlashSaleMapper;
import com.allan.model.FlashSale;
import com.allan.model.Product;
import com.allan.repository.FlashSaleRepository;
import com.allan.repository.ProductRepository;
import com.allan.service.FlashSaleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlashSaleServiceImpl implements FlashSaleService {

    private final FlashSaleRepository flashSaleRepository;
    private final ProductRepository productRepository;
    private final FlashSaleMapper flashSaleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FlashSaleDTO> getAllFlashSales() throws Exception {
        return flashSaleMapper.toDTOList(flashSaleRepository.findAllWithProducts());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlashSaleDTO> getCurrentlyLiveFlashSales() throws Exception {
        return flashSaleMapper.toDTOList(
                flashSaleRepository.findCurrentlyLive(LocalDateTime.now()));
    }

    @Override
    @Transactional
    public FlashSaleDTO createFlashSale(String title, Integer discountPercent,
                                         LocalDateTime startTime, LocalDateTime endTime,
                                         List<Long> productIds) throws Exception {

        if (startTime.isAfter(endTime)) {
            throw new Exception("Flash sale start time must be before end time.");
        }
        if (discountPercent == null || discountPercent < 0 || discountPercent > 100) {
            throw new Exception("Discount percent must be between 0 and 100.");
        }

        FlashSale flashSale = new FlashSale();
        flashSale.setTitle(title);
        flashSale.setDiscountPercent(discountPercent);
        flashSale.setStartTime(startTime);
        flashSale.setEndTime(endTime);
        flashSale.setActive(true);
        flashSale.setCreatedAt(LocalDateTime.now());
        flashSale.setProducts(resolveProducts(productIds));

        FlashSale saved = flashSaleRepository.save(flashSale);
        log.info("Flash sale created: id={} title={} products={}",
                saved.getId(), saved.getTitle(), saved.getProducts().size());

        return flashSaleMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public FlashSaleDTO addProductsToFlashSale(Long flashSaleId, List<Long> productIds) throws Exception {
        FlashSale flashSale = flashSaleRepository.findByIdWithProducts(flashSaleId)
                .orElseThrow(() -> new Exception("Flash sale not found with id: " + flashSaleId));

        flashSale.getProducts().addAll(resolveProducts(productIds));
        flashSale.setUpdatedAt(LocalDateTime.now());

        FlashSale saved = flashSaleRepository.save(flashSale);
        log.info("Added {} products to flash sale {}", productIds.size(), flashSaleId);

        return flashSaleMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void deactivateFlashSale(Long flashSaleId) throws Exception {
        FlashSale flashSale = flashSaleRepository.findById(flashSaleId)
                .orElseThrow(() -> new Exception("Flash sale not found with id: " + flashSaleId));
        flashSale.setActive(false);
        flashSale.setUpdatedAt(LocalDateTime.now());
        flashSaleRepository.save(flashSale);
        log.info("Flash sale {} deactivated", flashSaleId);
    }

    private Set<Product> resolveProducts(List<Long> productIds) throws Exception {
        Set<Product> products = productIds.stream()
                .map(id -> productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id)))
                .collect(Collectors.toSet());

        if (products.size() != productIds.size()) {
            throw new Exception("One or more product IDs could not be resolved.");
        }
        return products;
    }
}