package com.mygo.controller.admin;

import com.mygo.dto.AdminAddScheduleDTO;
import com.mygo.result.Result;
import com.mygo.service.AdminService;
import com.mygo.vo.AdminScheduleVO;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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
    Result<AdminScheduleVO> GetScheduleByCounselor(@PathVariable Integer counselorId, @RequestParam Date startDate,
                                                   @RequestParam Date endDate) {
        AdminScheduleVO scheduleByCounselor = adminService.getScheduleByCounselor(startDate, endDate);
        return Result.success(scheduleByCounselor);
    }

    @PostMapping("/api/schedules/{scheduleId}/daily/{date}/approve")
    Result<Void> ApproveScheduleByDay(@PathVariable Integer scheduleId, @PathVariable Date date) {
        adminService.approveScheduleByDay(scheduleId);
        return Result.success();
    }

    @PostMapping("/schedules/{scheduleId}/daily/{date}/timeslot/{timeSlotId}/approve")
    Result<Void> ApproveScheduleByTimeSlot(@PathVariable Integer scheduleId, @PathVariable Date date,
                                           @PathVariable Integer timeSlotId) {
        adminService.approveScheduleByTimeSlot(timeSlotId);
        return Result.success();
    }

    @GetMapping("/api/schedules/supervisor/{supervisorId}/weekly")
    Result<List<AdminScheduleVO>> GetScheduleBySupervisor(@PathVariable Integer supervisorId, @RequestParam Date startDate,
                                                          @RequestParam Date endDate) {
        List<AdminScheduleVO> scheduleBySupervisor = adminService.getScheduleBySupervisor(startDate, endDate);
        return Result.success(scheduleBySupervisor);
    }

    @GetMapping("/api/schedules/weekly")
    Result<List<AdminScheduleVO>> GetScheduleByManager(@RequestParam Date startDate,
                                                          @RequestParam Date endDate) {
        List<AdminScheduleVO> scheduleByManager = adminService.getScheduleByManager(startDate, endDate);
        return Result.success(scheduleByManager);
    }

}
