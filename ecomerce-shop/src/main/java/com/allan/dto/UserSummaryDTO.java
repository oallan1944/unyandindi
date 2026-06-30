// src/main/java/com/allan/dto/UserSummaryDTO.java
package com.allan.dto;

import lombok.Data;

@Data
public class UserSummaryDTO {
    private Long id;
    private String email;
    private String fullName;
    private String mobile;
    // ✅ no addresses, no usedCoupons, no password — only what's needed
}