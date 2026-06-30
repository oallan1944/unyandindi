package com.allan.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FlashSaleDTO {
    private Long id;
    private String title;
    private Integer discountPercent;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean active;
    private boolean currentlyLive;
    private List<ProductInFlashSaleDTO> products; // ✅ was List<ProductSummaryDTO>
}
