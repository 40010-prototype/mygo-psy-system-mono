package com.mygo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.dto.MessageFromToDTO;
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

    /**
     * 把消息存到数据库，并调用sendMessage方法转发该消息。
     * @param messageFromToDTO 消息实体对象，包括如下字段：<br>
     *                消息发送者，消息接收者，消息类型，消息
     * @throws JsonProcessingException 序列化异常
     */
    @Override
    public void receiveMessage(MessageFromToDTO messageFromToDTO) throws JsonProcessingException {
        //1.判断数据来源
        boolean isFromUser = messageFromToDTO.getFromId()
                .split("_")[0].equals("user");
        Integer toId = Integer.valueOf(messageFromToDTO.getToId()
                .split("_")[1]);
        Integer fromId = Integer.valueOf(messageFromToDTO.getFromId()
                .split("_")[1]);
        if (isFromUser) {
            //if和else中逻辑相同，这里只作一处注释。
            //2.根据发送方和接收方查询消息对应的咨询id
            Integer consultId = chatMapper.getConsultId(toId, fromId);
            //3.根据咨询id插入这条消息
            chatMapper.addMessage(consultId, messageFromToDTO.getMessage(), messageFromToDTO.getMessageType(), "user");
        } else {
            Integer consultId = chatMapper.getConsultId(fromId, toId);
            chatMapper.addMessage(consultId, messageFromToDTO.getMessage(), messageFromToDTO.getMessageType(),
                    "counselor");
        }

    }

    @Override
    public Integer getMessageId(MessageFromToDTO messageFromToDTO) {
        boolean isFromUser = messageFromToDTO.getFromId()
                .split("_")[0].equals("user");
        Integer toId = Integer.valueOf(messageFromToDTO.getToId()
                .split("_")[1]);
        Integer fromId = Integer.valueOf(messageFromToDTO.getFromId()
                .split("_")[1]);
        Integer messageId;
        if (isFromUser) {
            messageId = chatMapper.getMessageId(toId, fromId);
        } else {
            messageId = chatMapper.getMessageId(fromId, toId);
        }
        return messageId;
    }

    @Override
    public Integer getConsultId(MessageFromToDTO messageFromToDTO) {
        boolean isFromUser = messageFromToDTO.getFromId()
                .split("_")[0].equals("user");
        Integer toId = Integer.valueOf(messageFromToDTO.getToId()
                .split("_")[1]);
        Integer fromId = Integer.valueOf(messageFromToDTO.getFromId()
                .split("_")[1]);
        Integer consultId;
        if (isFromUser) {
            consultId = chatMapper.getConsultId(toId, fromId);
        } else {
            consultId = chatMapper.getConsultId(fromId, toId);
        }
        return consultId;
    }

}
