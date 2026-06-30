package com.allan.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateFlashSaleRequest {
    private String title;
    private Integer discountPercent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Long> productIds;
}