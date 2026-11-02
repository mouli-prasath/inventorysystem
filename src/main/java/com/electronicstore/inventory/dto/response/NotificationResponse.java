package com.electronicstore.inventory.dto.response;

import com.electronicstore.inventory.enums.NotificationPriority;
import com.electronicstore.inventory.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationPriority priority;
    private boolean isRead;
    private String reference;
    private LocalDateTime createdDate;
}
