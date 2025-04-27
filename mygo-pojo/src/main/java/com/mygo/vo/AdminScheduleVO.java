package com.mygo.vo;

import com.mygo.dto.ScheduleAndStatusDTO;
import com.mygo.enumeration.ScheduleStatus;
import lombok.Data;

import java.util.List;

@Data
public class AdminScheduleVO {

    String id;

    String counselorName;

    List<ScheduleAndStatusDTO> schedules;

    ScheduleStatus overallStatus;

}
