// src/main/java/com/allan/dto/AddressDTO.java
package com.allan.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String mobile;
    private String locality;
}