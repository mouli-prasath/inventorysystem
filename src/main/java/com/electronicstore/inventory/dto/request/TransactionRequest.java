package com.electronicstore.inventory.dto.request;

import com.electronicstore.inventory.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TransactionRequest {

    private Long customerId; // optional - walk-in sale if omitted
    private String walkInFirstName;
    private String walkInLastName;
    private String walkInPhone;
    private String walkInEmail;
    @NotBlank(message = "Store location is required")
    private String storeLocation;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.0", message = "Tax rate cannot be negative")
    private BigDecimal taxRate; // e.g. 0.08 for 8%; defaults to 0 if omitted

    @NotEmpty(message = "Transaction must include at least one item")
    @Valid
    private List<TransactionItemRequest> items;

    private String notes;
}
