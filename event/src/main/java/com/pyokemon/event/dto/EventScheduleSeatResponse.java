package com.pyokemon.event.dto;

import java.util.List;

public class EventScheduleSeatResponse {
    private Long eventScheduleId;
    private List<SeatGradeRemaining> remainingSeatsByGrade; // 이름 변경
    private List<SeatMapDetail> seatMap;

    // Getters and Setters
    public Long getEventScheduleId() { return eventScheduleId; }
    public void setEventScheduleId(Long eventScheduleId) { this.eventScheduleId = eventScheduleId; }
    public List<SeatGradeRemaining> getRemainingSeatsByGrade() { return remainingSeatsByGrade; }
    public void setRemainingSeatsByGrade(List<SeatGradeRemaining> remainingSeatsByGrade) { this.remainingSeatsByGrade = remainingSeatsByGrade; }
    public List<SeatMapDetail> getSeatMap() { return seatMap; }
    public void setSeatMap(List<SeatMapDetail> seatMap) { this.seatMap = seatMap; }
}