// src/main/java/com/allan/dto/BankDetailsDTO.java
package com.allan.dto;

import lombok.Data;

@Data
public class BankDetailsDTO {
    private String accountHolderName;
    private String accountNumber;
    private String ifscCode;
}