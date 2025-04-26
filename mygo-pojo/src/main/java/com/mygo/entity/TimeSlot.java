package com.mygo.entity;

import com.mygo.enumeration.TimeStatus;

import java.time.LocalTime;

public class TimeSlot {
    private String id;
    private LocalTime startTime;
    private LocalTime endTime;
    private TimeStatus status;
}
