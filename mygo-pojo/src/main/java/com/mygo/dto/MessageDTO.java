package com.mygo.dto;

import com.mygo.enumeration.MessageType;
import lombok.Data;

@Data
public class MessageDTO {

    Integer toId;

    MessageType messageType;

    String message;

}
