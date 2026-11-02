package com.electronicstore.inventory.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    private String contactPerson;

    @Email(message = "Please enter a valid email address")
    private String email;

    private String phone;

    private String address;

    private String paymentTerms;
}
