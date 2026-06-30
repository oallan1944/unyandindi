package com.allan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.allan.dto.FlashSaleDTO;
import com.allan.request.CreateFlashSaleRequest;
import com.allan.service.FlashSaleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Owns the full lifecycle of the FlashSale resource.
 *  - Public read under /home/flash-sales — no auth, same audience as GET /home.
 *  - Admin mutation under /api/admin/flash-sales — gated by the existing
 *    /api/admin/** -> ROLE_ADMIN wildcard in SecurityConfig; no new rule needed.
 *
 * Flash sales are admin-curated only in the current design: an admin selects
 * which products (from any seller) belong to a campaign. There is no
 * seller-nomination or approval workflow.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    // ═════════════════════════════════════════════════════════════
    // PUBLIC — customer-facing, no auth
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/home/flash-sales")
    public ResponseEntity<List<FlashSaleDTO>> getLiveFlashSales() throws Exception {
        return ResponseEntity.ok(flashSaleService.getCurrentlyLiveFlashSales());
    }

    // ═════════════════════════════════════════════════════════════
    // ADMIN — ROLE_ADMIN only, enforced by SecurityConfig
    // ═════════════════════════════════════════════════════════════

    @GetMapping("/api/admin/flash-sales")
    public ResponseEntity<List<FlashSaleDTO>> getAllFlashSales() throws Exception {
        return ResponseEntity.ok(flashSaleService.getAllFlashSales());
    }

    @PostMapping("/api/admin/flash-sales")
    public ResponseEntity<FlashSaleDTO> createFlashSale(
            @RequestBody CreateFlashSaleRequest req) throws Exception {

        if (req.getTitle() == null || req.getTitle().isBlank()) {
            log.warn("Flash sale creation attempted with blank title");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (req.getStartTime() == null || req.getEndTime() == null) {
            log.warn("Flash sale creation attempted with missing start/end time");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (req.getProductIds() == null || req.getProductIds().isEmpty()) {
            log.warn("Flash sale creation attempted with no products");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        FlashSaleDTO created = flashSaleService.createFlashSale(
                req.getTitle(),
                req.getDiscountPercent(),
                req.getStartTime(),
                req.getEndTime(),
                req.getProductIds()
        );

        log.info("Flash sale created: id={} title={} products={}",
                created.getId(), created.getTitle(), req.getProductIds().size());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/api/admin/flash-sales/{id}/products")
    public ResponseEntity<FlashSaleDTO> addProductsToFlashSale(
            @PathVariable Long id,
            @RequestBody List<Long> productIds) throws Exception {

        if (productIds == null || productIds.isEmpty()) {
            log.warn("Add-products attempted with empty list for flash sale {}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        FlashSaleDTO updated = flashSaleService.addProductsToFlashSale(id, productIds);
        log.info("Added {} products to flash sale {}", productIds.size(), id);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/api/admin/flash-sales/{id}/deactivate")
    public ResponseEntity<Void> deactivateFlashSale(@PathVariable Long id) throws Exception {
        flashSaleService.deactivateFlashSale(id);
        log.info("Flash sale {} deactivated", id);
        return ResponseEntity.noContent().build();
    }
}