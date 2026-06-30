package com.allan.dto;

import lombok.Data;

@Data
public class ProductInFlashSaleDTO {
    private ProductSummaryDTO product;

    /** The product's normal selling price, untouched by this campaign. */
    private double originalPrice;

    /** originalPrice minus the flash sale's discountPercent — only meaningful while the sale is live. */
    private double effectivePrice;

    /** Convenience for the frontend — avoids re-deriving "how much are they actually saving". */
    private double amountSaved;
}