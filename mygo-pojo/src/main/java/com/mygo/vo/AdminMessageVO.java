package com.mygo.vo;

import com.mygo.enumeration.MessageStatus;
import com.mygo.enumeration.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class AdminMessageVO {

    private String id;

    private String senderId;

    private String receiverId;

    private String sessionId;

    private String content;

    private LocalDateTime timestamp;

    private MessageType type;

    private MessageStatus status;

    private Map<String, Object> meta;

}
