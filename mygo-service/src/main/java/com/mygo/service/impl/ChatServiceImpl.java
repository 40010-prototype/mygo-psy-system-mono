package com.mygo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.domain.entity.Message;
import com.mygo.mapper.ChatMapper;
import com.mygo.service.ChatService;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final ChatMapper chatMapper;

    public ChatServiceImpl(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;
    }

    @Override
    public void receiveMessage(Message message) {
        //判断数据来源
        boolean isFromUser = message.getFromId()
                .split("_")[0].equals("user");
        Integer toId=Integer.valueOf(message.getToId()
                .split("_")[1]);
        Integer fromId=Integer.valueOf(message.getFromId().split("_")[1]);
        if (isFromUser) {
            Integer consultId = chatMapper.getConsultId(toId, fromId);
            chatMapper.addMessage(consultId, message.getMessage(), message.getMessageType(),"user");
        } else {
            Integer consultId=chatMapper.getConsultId(fromId, toId);
            chatMapper.addMessage(consultId, message.getMessage(), message.getMessageType(),"admin");
        }
    }

}
