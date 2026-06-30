package com.allan.request;

import lombok.Data;

@Data
public class UpdateSellerRequest {

    private String sellerName;
    private String email;
    private String mobile;
    private String gstin;
    private String businessName;
    private String pickupAddress;
    private String accountNumber;
    private String ifscCode;

    // You can optionally add validation annotations here, if using Hibernate
    // Validator
    // @NotBlank, @Email, @Pattern, etc.
}
