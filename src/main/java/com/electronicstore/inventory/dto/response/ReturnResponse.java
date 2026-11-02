package com.electronicstore.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponse {
    private Long id;
    private Long transactionId;
    private String transactionNumber;
    private Long productId;
    private String productName;
    private Integer quantity;
    private String reason;
    private LocalDateTime returnDate;
    private BigDecimal refundAmount;
}
