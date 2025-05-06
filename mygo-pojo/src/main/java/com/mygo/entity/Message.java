package com.mygo.entity;

import com.mygo.enumeration.MessageStatus;
import com.mygo.enumeration.MessageType;
import com.mygo.enumeration.Sender;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Getter
public class Message {

    private Integer id;

    private Integer consultId;

    private MessageType messageType;

    private String message;

    private Sender sender;

    private String meta;

    private MessageStatus status;

    private LocalDateTime time;

}
