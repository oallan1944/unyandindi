// src/main/java/com/allan/dto/OrderItemDTO.java
package com.allan.dto;
import com.allan.dto.ProductSummaryDTO;
import lombok.Data;

@Data
public class OrderItemDTO {
    private Long id;
    private int quantity;
    private int mrpPrice;
    private int sellingPrice;
    private String size;

    // ✅ only product summary — not full product with reviews/seller/category
    private ProductSummaryDTO product;
}