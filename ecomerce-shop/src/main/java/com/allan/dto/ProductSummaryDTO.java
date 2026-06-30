// src/main/java/com/allan/dto/ProductSummaryDTO.java
package com.allan.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductSummaryDTO {
    private Long id;
    private String title;
    private int mrpPrice;
    private int sellingPrice;
    private int discountPercent;
    private List<String> images;
    private String color;
}
