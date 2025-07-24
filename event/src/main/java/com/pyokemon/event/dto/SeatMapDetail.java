package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapDetail {
    private Long seatId;
    private String row;
    private String col;
    private String seatGrade;
    private boolean isBooked;

    public Long getSeatId() { return seatId; }
    public void setSeatId(Long seatId) { this.seatId = seatId; }
    public String getRow() { return row; }
    public void setRow(String row) { this.row = row; }
    public String getCol() { return col; }
    public void setCol(String col) { this.col = col; }
    public String getSeatGrade() { return seatGrade; }
    public void setSeatGrade(String seatGrade) { this.seatGrade = seatGrade; }
    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }
}