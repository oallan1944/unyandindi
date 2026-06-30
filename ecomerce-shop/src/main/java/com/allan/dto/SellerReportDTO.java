// src/main/java/com/allan/dto/SellerReportDTO.java
package com.allan.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SellerReportDTO {
    private SellerSummaryDTO seller;
    private Long totalOrders;
    private Long completedOrders;
    private Long pendingOrders;
    private Long canceledOrders;
    private Double totalRevenue;
    private Long totalProducts;
    private LocalDateTime generatedAt;
}
