package com.mygo.dto;

import com.mygo.enumeration.MessageType;
import lombok.Data;

import java.util.Map;

@Data
public class MessageDTO {

    Integer toId;

    MessageType messageType;

    String message;

    Map<String, Object> meta;

}
