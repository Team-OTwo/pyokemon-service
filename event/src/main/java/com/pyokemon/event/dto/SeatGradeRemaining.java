package com.pyokemon.event.dto;

public class SeatGradeRemaining {
    private String seatGrade;
    private Integer remainingSeats;

    // Getters and Setters
    public String getSeatGrade() { return seatGrade; }
    public void setSeatGrade(String seatGrade) { this.seatGrade = seatGrade; }
    public Integer getRemainingSeats() { return remainingSeats; }
    public void setRemainingSeats(Integer remainingSeats) { this.remainingSeats = remainingSeats; }
}