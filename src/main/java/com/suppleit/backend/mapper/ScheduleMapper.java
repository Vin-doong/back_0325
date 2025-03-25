package com.suppleit.backend.mapper;

import com.suppleit.backend.model.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ScheduleMapper {
    // 스케줄 등록
    void insertSchedule(Schedule schedule);
    
    // 스케줄 목록 조회 (특정 회원)
    List<Schedule> getSchedulesByMemberId(@Param("memberId") Long memberId);
    
    // 일별 스케줄 조회
    List<Schedule> getDailySchedules(@Param("memberId") Long memberId, @Param("date") LocalDate date);
    
    // 주간 스케줄 조회
    List<Schedule> getWeeklySchedules(
        @Param("memberId") Long memberId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );
    
    // 스케줄 상세 조회
    Schedule getScheduleById(@Param("scheduleId") Long scheduleId);
    
    // 스케줄 수정
    void updateSchedule(Schedule schedule);
    
    // 스케줄 삭제
    void deleteSchedule(@Param("scheduleId") Long scheduleId);
}