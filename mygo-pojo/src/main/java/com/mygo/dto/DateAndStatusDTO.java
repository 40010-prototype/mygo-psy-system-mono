package com.mygo.dto;

import com.mygo.enumeration.ScheduleStatus;
import lombok.Getter;

import java.util.Date;

@Getter
public class DateAndStatusDTO {

    private Date date;

    private ScheduleStatus status;

}
