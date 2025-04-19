package com.mygo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.dto.MessageFromToDTO;

public interface ChatService {

    void receiveMessage(MessageFromToDTO messageFromToDTO) throws JsonProcessingException;

    Integer getMessageId(MessageFromToDTO messageFromToDTO);

    Integer getConsultId(MessageFromToDTO messageFromToDTO);

}
