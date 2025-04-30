package com.mygo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mygo.enumeration.MessageType;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminMessageDTO {

    String receiverId;

    MessageType type;

    String content;

    Map<String, Object> meta;

}
