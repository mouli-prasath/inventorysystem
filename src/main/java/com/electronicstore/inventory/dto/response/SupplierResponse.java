package com.electronicstore.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponse {
    private Long id;
    private String supplierName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String paymentTerms;
    private boolean isActive;
}
