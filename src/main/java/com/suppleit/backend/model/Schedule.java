package com.suppleit.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {
    private Long scheduleId;
    private LocalDate intakeStart;
    private Integer intakeDistance;
    private LocalDate intakeEnd;
    private String intakeTime; // 복용 시간대: 아침, 점심, 저녁
    private String memo;
    private Long memberId;
    private Long prdId;
    
    // 추가 필드 - 제품명 (조회 시 JOIN에 사용)
    private String productName;
}