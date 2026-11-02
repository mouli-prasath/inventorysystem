package com.electronicstore.inventory.dto.request;

import com.electronicstore.inventory.enums.NotificationPriority;
import com.electronicstore.inventory.enums.NotificationType;
import com.electronicstore.inventory.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SendNotificationRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private String title;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    private NotificationPriority priority;

    /** Specific recipients by user id. If empty and roles is empty, broadcasts to all active users. */
    private List<Long> userIds;

    /** Send to every active user holding any of these roles. */
    private List<Role> roles;
}
