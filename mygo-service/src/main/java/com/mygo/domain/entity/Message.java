package com.mygo.domain.entity;

import com.mygo.domain.enumeration.MessageType;
import lombok.Data;

@Data
public class Message {
    Long fromId;
    Long toId;
    MessageType messageType;
    String message;
}
