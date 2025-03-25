package com.suppleit.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    private Long scheduleId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate intakeStart;
    
    private Integer intakeDistance;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate intakeEnd;
    
    private String intakeTime; // 단일 시간대 (아침, 점심, 저녁)
    private List<String> intakeTimes; // 여러 시간대를 한번에 등록할 때 사용
    private String memo;
    
    private Long memberId;
    private Long prdId;
    private String productName;
}