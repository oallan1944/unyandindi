package com.allan.dto;

import com.allan.domain.AccountStatus;
import com.allan.domain.USER_ROLE;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SellerProfileDTO {
    private Long id;
    private String sellerName;
    private String email;
    private String mobile;
    private String GSTIN;
    private USER_ROLE role;
    private boolean emailVerified;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private BusinessDetailsDTO businessDetails;
    private BankDetailsDTO bankDetails;     // ✅ only visible to the seller themselves
    private AddressDTO pickupAddress;
    // ❌ no password
}