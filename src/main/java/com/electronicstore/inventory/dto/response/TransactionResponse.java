package com.electronicstore.inventory.dto.response;

import com.electronicstore.inventory.enums.PaymentMethod;
import com.electronicstore.inventory.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String transactionNumber;
    private Long customerId;
    private String customerName;
    private Long employeeId;
    private String employeeName;
    private String storeLocation;
    private LocalDateTime transactionDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private TransactionStatus status;
    private String notes;
    private List<TransactionItemResponse> items;
}
