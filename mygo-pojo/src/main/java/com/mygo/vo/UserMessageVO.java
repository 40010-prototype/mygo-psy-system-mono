package com.mygo.vo;

import com.mygo.enumeration.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserMessageVO {

    Integer fromId;

    MessageType messageType;

    String message;

    LocalDateTime time;

}
