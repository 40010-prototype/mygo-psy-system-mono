package com.mygo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.domain.entity.Message;
import com.mygo.service.ChatService;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void receiveMessage(String message) throws JsonProcessingException {
        Message msg = mapper.readValue(message, Message.class);
        System.out.println(msg.getMessageType());
    }
}
