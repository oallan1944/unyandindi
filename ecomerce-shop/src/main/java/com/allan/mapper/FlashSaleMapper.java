package com.allan.mapper;

import com.allan.dto.FlashSaleDTO;
import com.allan.dto.ProductInFlashSaleDTO;
import com.allan.dto.ProductSummaryDTO;
import com.allan.model.FlashSale;
import com.allan.model.Product;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.stream.Collectors;

@Component
public class FlashSaleMapper {

    public FlashSaleDTO toDTO(FlashSale flashSale) {
        FlashSaleDTO dto = new FlashSaleDTO();
        dto.setId(flashSale.getId());
        dto.setTitle(flashSale.getTitle());
        dto.setDiscountPercent(flashSale.getDiscountPercent());
        dto.setStartTime(flashSale.getStartTime());
        dto.setEndTime(flashSale.getEndTime());
        dto.setActive(flashSale.isActive());
        dto.setCurrentlyLive(flashSale.isCurrentlyLive());

        dto.setProducts(flashSale.getProducts().stream()
                .map(p -> toProductInFlashSale(p, flashSale))
                .collect(Collectors.toList()));

        return dto;
    }

    public List<FlashSaleDTO> toDTOList(List<FlashSale> sales) {
        return sales.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * The single source of truth for "what does this product cost during this campaign".
     * Product.sellingPrice is never mutated — this is purely a display-time calculation.
     */
    private ProductInFlashSaleDTO toProductInFlashSale(Product product, FlashSale flashSale) {
        ProductInFlashSaleDTO dto = new ProductInFlashSaleDTO();
        dto.setProduct(toProductSummary(product));

        double originalPrice = product.getSellingPrice();

        // ✅ effective price only reflects the discount while the campaign is actually live —
        // an expired or deactivated sale shows the unmodified original price, never a stale discount.
        double effectivePrice = flashSale.isCurrentlyLive()
                ? originalPrice * (1 - flashSale.getDiscountPercent() / 100.0)
                : originalPrice;

        dto.setOriginalPrice(originalPrice);
        dto.setEffectivePrice(Math.round(effectivePrice * 100.0) / 100.0); // 2dp rounding
        dto.setAmountSaved(Math.round((originalPrice - effectivePrice) * 100.0) / 100.0);

        return dto;
    }

    private ProductSummaryDTO toProductSummary(Product p) {
        ProductSummaryDTO dto = new ProductSummaryDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setMrpPrice(p.getMrpPrice());
        dto.setSellingPrice(p.getSellingPrice());
        dto.setDiscountPercent(p.getDiscountPercent());
        dto.setImages(p.getImages());
        dto.setColor(p.getColor());
        return dto;
    }
}
