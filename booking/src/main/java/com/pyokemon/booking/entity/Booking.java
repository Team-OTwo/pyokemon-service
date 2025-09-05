package com.pyokemon.booking.entity;


import com.pyokemon.common.entity.BaseEntity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseEntity {

  private Long eventScheduleId;
  private Long seatId;
  private Long accountId;
  private Long tenantId;
  private Long paymentId;
  private Booked status;

  public enum Booked {
    PENDING, BOOKED, CANCELED, FAILED, EXPIRED
  }
}
