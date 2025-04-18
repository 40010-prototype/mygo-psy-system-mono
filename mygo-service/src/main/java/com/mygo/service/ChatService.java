package com.mygo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.entity.Message;

public interface ChatService {

    void receiveMessage(Message message) throws JsonProcessingException;

    Integer getMesaageId();

}
