package com.pyokemon.notification.event.consumer.message.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private Long bookingId;
    private Long eventScheduleId;
    private Long seatId;
    private Long accountId;
    private Long tenantId;
    private String status;
}
