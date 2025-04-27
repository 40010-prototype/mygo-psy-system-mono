package com.mygo.controller.admin;

import com.mygo.dto.AdminAddScheduleDTO;
import com.mygo.result.Result;
import com.mygo.service.AdminService;
import com.mygo.vo.AdminScheduleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
public class ScheduleController {

    private final AdminService adminService;

    public ScheduleController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/api/schedules")
    Result<Void> AddSchedule(@RequestBody AdminAddScheduleDTO adminAddScheduleDTO) {
        adminService.addSchedule(adminAddScheduleDTO);
        return Result.success();
    }

    @GetMapping("/api/schedules/counselor/{counselorId}/weekly")
    Result<AdminScheduleVO> GetScheduleByCounselor(@PathVariable Integer counselorId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")Date startDate,
                                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        log.info(counselorId.toString());
        AdminScheduleVO scheduleByCounselor = adminService.getScheduleByCounselor(startDate, endDate);
        return Result.success(scheduleByCounselor);
    }

    @PostMapping("/api/schedules/{scheduleId}/daily/{date}/approve")
    Result<Void> ApproveScheduleByDay(@PathVariable Integer scheduleId, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        adminService.approveScheduleByDay(scheduleId);
        return Result.success();
    }

    @PostMapping("/schedules/{scheduleId}/daily/{date}/timeslot/{timeSlotId}/approve")
    Result<Void> ApproveScheduleByTimeSlot(@PathVariable Integer scheduleId, @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                           @PathVariable Integer timeSlotId) {
        adminService.approveScheduleByTimeSlot(timeSlotId);
        return Result.success();
    }

    @GetMapping("/api/schedules/supervisor/{supervisorId}/weekly")
    Result<List<AdminScheduleVO>> GetScheduleBySupervisor(@PathVariable Integer supervisorId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                          @RequestParam  @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<AdminScheduleVO> scheduleBySupervisor = adminService.getScheduleBySupervisor(startDate, endDate);
        return Result.success(scheduleBySupervisor);
    }

    @GetMapping("/api/schedules/weekly")
    Result<List<AdminScheduleVO>> GetScheduleByManager(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<AdminScheduleVO> scheduleByManager = adminService.getScheduleByManager(startDate, endDate);
        return Result.success(scheduleByManager);
    }

    @GetMapping("/api/schedules/pending/supervisor/{supervisorId}/weekly")
    Result<List<AdminScheduleVO>> GetPendingScheduleBySupervisor(@PathVariable Integer supervisorId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        List<AdminScheduleVO> scheduleBySupervisor = adminService.getPendingScheduleBySupervisor(startDate, endDate);
        return Result.success(scheduleBySupervisor);
    }

}
