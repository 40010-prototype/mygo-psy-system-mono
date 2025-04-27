package com.mygo.entity;

import com.mygo.dto.TimeSlotDTO;
import com.mygo.enumeration.ScheduleStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Schedule {

    private Date date;

    private List<TimeSlotDTO> timeSlots;

    private ScheduleStatus approvalStatus;

    private String approvalRemark;

}
