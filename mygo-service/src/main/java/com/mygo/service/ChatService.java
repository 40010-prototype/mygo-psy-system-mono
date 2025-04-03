package com.mygo.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ChatService {

    void receiveMessage(String message) throws JsonProcessingException;
}
