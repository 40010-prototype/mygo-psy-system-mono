package com.mygo.dto;

import com.mygo.enumeration.ScheduleStatus;
import com.mygo.enumeration.TimeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class TimeSlotDTO {

    private String id;

    private LocalTime startTime;

    private LocalTime endTime;

    private TimeStatus status;

    private ScheduleStatus approvalStatus;

    private String remark;

}
