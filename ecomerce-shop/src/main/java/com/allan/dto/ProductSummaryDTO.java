// src/main/java/com/allan/dto/ProductSummaryDTO.java
package com.allan.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProductSummaryDTO {
    private Long id;
    private String title;
    // Widened from int to long, matching Product.java.
    private long mrpPrice;
    private long sellingPrice;
    private int discountPercent;
    private List<String> images;
    private String color;
}