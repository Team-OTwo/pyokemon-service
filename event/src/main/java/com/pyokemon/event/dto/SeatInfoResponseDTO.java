package com.pyokemon.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatInfoResponseDTO {
  private Long seatId;
  private String col;
  private String row;
  private String seatGrade;
  private String floor;
  
  // SQL alias와 매핑을 위한 필드들
  private String colValue;
  private String rowValue;
  
  // SQL 결과를 DTO 필드에 매핑하는 메서드
  public void setColValue(String colValue) {
    this.colValue = colValue;
    this.col = colValue; // col 필드에도 동일한 값 설정
  }
  
  public void setRowValue(String rowValue) {
    this.rowValue = rowValue;
    this.row = rowValue; // row 필드에도 동일한 값 설정
  }
}
