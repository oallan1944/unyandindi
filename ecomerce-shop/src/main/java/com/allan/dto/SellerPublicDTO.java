// src/main/java/com/allan/dto/SellerPublicDTO.java
package com.allan.dto;

import lombok.Data;

@Data
public class SellerPublicDTO {
    private Long id;
    private String sellerName;
    private String businessName;
    private String logo;
    private String banner;
    // ❌ no email, password, bank details, mobile — public storefront only
}