package com.allan.request;

import lombok.Data;

@Data
public class AddItemRequest {

    private String size;
    private int quantity;
    private Long productId;
    private String color;
}
