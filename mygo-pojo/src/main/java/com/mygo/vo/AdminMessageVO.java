package com.mygo.vo;


import com.mygo.enumeration.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminMessageVO {

    String id;

    String senderId;

    String receiverId;

    String sessionID;

    String content;

    LocalDateTime timestamp;

    MessageType type;

    String status;

    String message;

}
