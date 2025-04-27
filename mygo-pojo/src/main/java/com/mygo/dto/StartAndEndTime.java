package com.mygo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class StartAndEndTime {

    LocalTime startTime;

    LocalTime endTime;

    public boolean conflict(StartAndEndTime startAndEndTime) {
        return !(startAndEndTime.getStartTime()
                .isAfter(endTime) || startAndEndTime.getEndTime()
                .isBefore(startTime));
    }

}
