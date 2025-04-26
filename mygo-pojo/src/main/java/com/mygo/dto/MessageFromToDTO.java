package com.mygo.dto;

import com.mygo.enumeration.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class MessageFromToDTO {

    String fromId;

    String toId;

    MessageType messageType;

    String message;

    Map<String, Object> meta;

    LocalDateTime time;

}
