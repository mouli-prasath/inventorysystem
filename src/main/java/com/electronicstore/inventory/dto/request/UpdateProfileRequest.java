package com.electronicstore.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** Self-service profile update - deliberately excludes username/email/role/employeeId,
 *  which require admin action (see AdminUserController) to change. */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Pattern(regexp = "^$|^[0-9]{10,15}$", message = "Invalid phone format")
    private String phoneNumber;

    private String storeLocation;
}
