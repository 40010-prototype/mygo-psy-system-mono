package com.mygo.entity;

import com.mygo.enumeration.ScheduleStatus;
import com.mygo.enumeration.TimeStatus;
import lombok.Data;

import java.time.LocalTime;

@Data
public class TimeSlot {

    private Integer id;

    private LocalTime startTime;

    private LocalTime endTime;

    private TimeStatus status;

    private ScheduleStatus approvalStatus;

    private String remark;

}
