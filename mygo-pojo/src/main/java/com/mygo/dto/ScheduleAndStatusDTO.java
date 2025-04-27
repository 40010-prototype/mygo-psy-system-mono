package com.mygo.dto;

import com.mygo.enumeration.ScheduleStatus;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ScheduleAndStatusDTO {

    private Date date;

    private List<TimeSlotDTO> timeSlots;

    private ScheduleStatus status;

}
