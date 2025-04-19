package com.mygo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LastMessageAndTime {

    String message;

    LocalDateTime time;

}
