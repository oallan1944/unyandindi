// src/main/java/com/allan/dto/SellerSummaryDTO.java
package com.allan.dto;

import com.allan.domain.AccountStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SellerSummaryDTO {
    private Long id;
    private String sellerName;
    private String email;
    private String mobile;
    private AccountStatus accountStatus;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    // ❌ no bank details — admin list view doesn't need financial data per row
}