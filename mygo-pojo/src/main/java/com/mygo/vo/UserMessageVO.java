package com.mygo.vo;

import com.mygo.enumeration.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class UserMessageVO {

    Integer fromId;

    Integer toId;

    MessageType messageType;

    String message;

    Map<String, Object> meta;

    LocalDateTime time;

}
