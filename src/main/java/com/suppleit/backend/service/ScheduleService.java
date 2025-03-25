package com.suppleit.backend.service;

import com.suppleit.backend.dto.ScheduleDto;
import com.suppleit.backend.mapper.MemberMapper;
import com.suppleit.backend.mapper.ScheduleMapper;
import com.suppleit.backend.model.Member;
import com.suppleit.backend.model.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final MemberMapper memberMapper;

    /**
     * 복용 일정 등록 - 여러 시간대를 한번에 처리
     */
    @Transactional
    public List<Long> createSchedules(ScheduleDto scheduleDto, String email) {
        Member member = memberMapper.getMemberByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다: " + email);
        }

        List<Long> createdIds = new ArrayList<>();
        
        // 복용 시간대가 여러 개인 경우 각각 등록
        List<String> intakeTimes = scheduleDto.getIntakeTimes();
        if (intakeTimes == null || intakeTimes.isEmpty()) {
            // 단일 시간대만 있는 경우
            if (scheduleDto.getIntakeTime() != null) {
                intakeTimes = List.of(scheduleDto.getIntakeTime());
            } else {
                throw new IllegalArgumentException("복용 시간대는 필수입니다.");
            }
        }

        // 각 시간대별로 일정 등록
        for (String intakeTime : intakeTimes) {
            Schedule schedule = new Schedule();
            schedule.setIntakeStart(scheduleDto.getIntakeStart());
            schedule.setIntakeDistance(scheduleDto.getIntakeDistance());
            schedule.setIntakeEnd(scheduleDto.getIntakeEnd());
            schedule.setIntakeTime(intakeTime);
            schedule.setMemo(scheduleDto.getMemo());
            schedule.setMemberId(member.getMemberId());
            schedule.setPrdId(scheduleDto.getPrdId());

            scheduleMapper.insertSchedule(schedule);
            createdIds.add(schedule.getScheduleId());
            
            log.info("복용 일정 등록 완료: {}, 시간대: {}", schedule.getScheduleId(), intakeTime);
        }

        return createdIds;
    }

    /**
     * 회원의 모든 복용 일정 조회
     */
    public List<ScheduleDto> getAllSchedules(String email) {
        Member member = memberMapper.getMemberByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다: " + email);
        }

        List<Schedule> schedules = scheduleMapper.getSchedulesByMemberId(member.getMemberId());
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 일별 복용 일정 조회
     */
    public List<ScheduleDto> getDailySchedules(String email, LocalDate date) {
        Member member = memberMapper.getMemberByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다: " + email);
        }

        List<Schedule> schedules = scheduleMapper.getDailySchedules(member.getMemberId(), date);
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 주간 복용 일정 조회
     */
    public Map<String, List<ScheduleDto>> getWeeklySchedules(String email, LocalDate weekStart) {
        Member member = memberMapper.getMemberByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다: " + email);
        }

        // 주의 시작일(월요일)과 종료일(일요일) 계산
        if (weekStart == null) {
            weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }
        LocalDate weekEnd = weekStart.plusDays(6);

        // 주간 일정 조회
        List<Schedule> schedules = scheduleMapper.getWeeklySchedules(
                member.getMemberId(), weekStart, weekEnd);

        // 요일별로 그룹화
        Map<String, List<ScheduleDto>> weeklySchedules = new HashMap<>();
        String[] daysOfWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        
        // 먼저 모든 요일에 대해 빈 리스트 초기화
        for (String day : daysOfWeek) {
            weeklySchedules.put(day, new ArrayList<>());
        }
        
        // 스케줄이 존재하는 날짜에 대해 정보 채우기
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            LocalDate currentDate = date;
            String dayOfWeek = date.getDayOfWeek().name();
            
            // 해당 날짜에 복용해야 하는 일정 필터링
            List<ScheduleDto> daySchedules = schedules.stream()
                    .filter(s -> isScheduleActiveOnDate(s, currentDate))
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            weeklySchedules.put(dayOfWeek, daySchedules);
        }
        
        return weeklySchedules;
    }

    /**
     * 특정 날짜에 일정이 활성화되어 있는지 확인
     */
    private boolean isScheduleActiveOnDate(Schedule schedule, LocalDate date) {
        // 시작일부터 종료일 사이에 있는지 확인
        return !date.isBefore(schedule.getIntakeStart()) && 
               (schedule.getIntakeEnd() == null || !date.isAfter(schedule.getIntakeEnd()));
    }

    /**
     * 복용 일정 상세 조회
     */
    public ScheduleDto getScheduleById(Long scheduleId, String email) {
        Member member = memberMapper.getMemberByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다: " + email);
        }

        Schedule schedule = scheduleMapper.getScheduleById(scheduleId);
        if (schedule == null || !schedule.getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("해당 일정을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        return convertToDto(schedule);
    }

    /**
     * 복용 일정 수정
     */
    @Transactional
    public void updateSchedule(Long scheduleId, ScheduleDto scheduleDto, String email) {
        Member member = memberMapper.getMemberByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다: " + email);
        }

        Schedule existingSchedule = scheduleMapper.getScheduleById(scheduleId);
        if (existingSchedule == null || !existingSchedule.getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("해당 일정을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        // 업데이트할 필드 설정
        Schedule schedule = new Schedule();
        schedule.setScheduleId(scheduleId);
        schedule.setIntakeStart(scheduleDto.getIntakeStart());
        schedule.setIntakeDistance(scheduleDto.getIntakeDistance());
        schedule.setIntakeEnd(scheduleDto.getIntakeEnd());
        schedule.setIntakeTime(scheduleDto.getIntakeTime());
        schedule.setMemo(scheduleDto.getMemo());
        schedule.setMemberId(member.getMemberId());
        schedule.setPrdId(scheduleDto.getPrdId());

        scheduleMapper.updateSchedule(schedule);
        log.info("복용 일정 수정 완료: {}", scheduleId);
    }

    /**
     * 복용 일정 삭제
     */
    @Transactional
    public void deleteSchedule(Long scheduleId, String email) {
        Member member = memberMapper.getMemberByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("회원을 찾을 수 없습니다: " + email);
        }

        Schedule schedule = scheduleMapper.getScheduleById(scheduleId);
        if (schedule == null || !schedule.getMemberId().equals(member.getMemberId())) {
            throw new IllegalArgumentException("해당 일정을 찾을 수 없거나 접근 권한이 없습니다.");
        }

        scheduleMapper.deleteSchedule(scheduleId);
        log.info("복용 일정 삭제 완료: {}", scheduleId);
    }

    /**
     * Entity -> DTO 변환
     */
    private ScheduleDto convertToDto(Schedule schedule) {
        return ScheduleDto.builder()
                .scheduleId(schedule.getScheduleId())
                .intakeStart(schedule.getIntakeStart())
                .intakeDistance(schedule.getIntakeDistance())
                .intakeEnd(schedule.getIntakeEnd())
                .intakeTime(schedule.getIntakeTime())
                .memo(schedule.getMemo())
                .memberId(schedule.getMemberId())
                .prdId(schedule.getPrdId())
                .productName(schedule.getProductName())
                .build();
    }
}