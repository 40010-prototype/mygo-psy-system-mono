package com.mygo.dto;

import com.mygo.entity.Schedule;
import lombok.Data;

import java.util.List;

@Data
public class AdminAddScheduleDTO {

    private String id;

    private String counselorName;

    private List<Schedule> schedules;

}
