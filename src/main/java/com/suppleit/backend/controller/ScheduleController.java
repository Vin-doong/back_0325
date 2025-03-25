package com.suppleit.backend.controller;

import com.suppleit.backend.dto.ApiResponse;
import com.suppleit.backend.dto.ScheduleDto;
import com.suppleit.backend.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController extends JwtSupportController {

    private final ScheduleService scheduleService;

    /**
     * 복용 일정 등록
     */
    @PostMapping
    public ResponseEntity<?> createSchedule(
            @RequestBody ScheduleDto scheduleDto,
            HttpServletRequest request) {
        try {
            String email = extractEmailFromToken(request);
            List<Long> scheduleIds = scheduleService.createSchedules(scheduleDto, email);
            return ResponseEntity.ok(ApiResponse.success("복용 일정이 등록되었습니다.", scheduleIds));
        } catch (Exception e) {
            log.error("복용 일정 등록 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("복용 일정 등록 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 복용 일정 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getAllSchedules(HttpServletRequest request) {
        try {
            String email = extractEmailFromToken(request);
            List<ScheduleDto> schedules = scheduleService.getAllSchedules(email);
            return ResponseEntity.ok(ApiResponse.success("조회 성공", schedules));
        } catch (Exception e) {
            log.error("복용 일정 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("복용 일정 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 오늘의 복용 일정 조회
     */
    @GetMapping("/daily")
    public ResponseEntity<?> getDailySchedules(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            HttpServletRequest request) {
        try {
            String email = extractEmailFromToken(request);
            date = date != null ? date : LocalDate.now();
            List<ScheduleDto> schedules = scheduleService.getDailySchedules(email, date);
            return ResponseEntity.ok(ApiResponse.success("조회 성공", schedules));
        } catch (Exception e) {
            log.error("일별 복용 일정 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("일별 복용 일정 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 주간 복용 일정 조회
     */
    @GetMapping("/weekly")
    public ResponseEntity<?> getWeeklySchedules(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate weekStart,
            HttpServletRequest request) {
        try {
            String email = extractEmailFromToken(request);
            Map<String, List<ScheduleDto>> schedules = scheduleService.getWeeklySchedules(email, weekStart);
            return ResponseEntity.ok(ApiResponse.success("조회 성공", schedules));
        } catch (Exception e) {
            log.error("주간 복용 일정 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("주간 복용 일정 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 복용 일정 상세 조회
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<?> getScheduleById(
            @PathVariable Long scheduleId,
            HttpServletRequest request) {
        try {
            String email = extractEmailFromToken(request);
            ScheduleDto schedule = scheduleService.getScheduleById(scheduleId, email);
            return ResponseEntity.ok(ApiResponse.success("조회 성공", schedule));
        } catch (Exception e) {
            log.error("복용 일정 상세 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("복용 일정 상세 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 복용 일정 수정
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleDto scheduleDto,
            HttpServletRequest request) {
        try {
            String email = extractEmailFromToken(request);
            scheduleService.updateSchedule(scheduleId, scheduleDto, email);
            return ResponseEntity.ok(ApiResponse.success("복용 일정이 수정되었습니다."));
        } catch (Exception e) {
            log.error("복용 일정 수정 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("복용 일정 수정 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 복용 일정 삭제
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(
            @PathVariable Long scheduleId,
            HttpServletRequest request) {
        try {
            String email = extractEmailFromToken(request);
            scheduleService.deleteSchedule(scheduleId, email);
            return ResponseEntity.ok(ApiResponse.success("복용 일정이 삭제되었습니다."));
        } catch (Exception e) {
            log.error("복용 일정 삭제 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("복용 일정 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}