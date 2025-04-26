package com.mygo.controller.admin;

import com.mygo.dto.AdminAddScheduleDTO;
import com.mygo.result.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class ScheduleController {


    @PostMapping("/api/schedules")
    Result<Void> AddSchedule(@RequestBody AdminAddScheduleDTO adminAddScheduleDTO) {
        return Result.success();
    }
}
