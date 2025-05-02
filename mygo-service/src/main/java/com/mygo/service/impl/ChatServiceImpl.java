package com.mygo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.dto.MessageFromToDTO;
import com.mygo.mapper.ChatMapper;
import com.mygo.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final ChatMapper chatMapper;

    public ChatServiceImpl(ChatMapper chatMapper) {
        this.chatMapper = chatMapper;

    }

    /**
     * 把消息存到数据库，并调用sendMessage方法转发该消息。
     * 
     * @param messageFromToDTO 消息实体对象，包括如下字段：<br>
     *                         消息发送者，消息接收者，消息类型，消息
     * @throws JsonProcessingException 序列化异常
     */
    @Override
    public void receiveMessage(MessageFromToDTO messageFromToDTO) throws JsonProcessingException {
        // 1.判断数据来源
        boolean isFromUser = messageFromToDTO.getFromId()
                .split("_")[0].equals("user");
        Integer toId = Integer.valueOf(messageFromToDTO.getToId()
                .split("_")[1]);
        Integer fromId = Integer.valueOf(messageFromToDTO.getFromId()
                .split("_")[1]);
        log.info("isFromUser: " + isFromUser);
        if (isFromUser) {
            // if和else中逻辑相同，这里只作一处注释。
            // 2.根据发送方和接收方查询消息对应的咨询id
            Integer consultId = chatMapper.getConsultId(toId, fromId);
            log.info("consultId: " + consultId);
            // 3.根据咨询id插入这条消息
            chatMapper.addMessage(consultId, messageFromToDTO.getMessage(), messageFromToDTO.getMessageType(), "user");
        } else {
            Integer consultId = chatMapper.getConsultId(fromId, toId);
            // 假如没找到，说明是督导-咨询师或者咨询师-督导
            // if (consultId == null) {
            // consultId = chatMapper.getConsultIdSupervisorConsult(toId, fromId) == null
            // ? chatMapper.getConsultIdSupervisorConsult(fromId, toId)
            // : chatMapper.getConsultIdSupervisorConsult(toId, fromId);
            // }
            String sender = "admin";
            if (consultId == null) {
                log.debug("未找到用户-咨询师会话ID，现在检查管理员-管理员会话 ({} 和 {})", fromId, toId);

                // 先查一个方向 (比如 toId, fromId) 并存储结果
                Integer adminConsultId = chatMapper.getConsultIdSupervisorConsult(toId, fromId);

                if (adminConsultId == null) {
                    // 如果第一个方向没找到，再查另一个方向 (fromId, toId)
                    log.debug("管理员会话 ({}, {}) 未找到，尝试查找 ({}, {})", toId, fromId, fromId, toId);
                    adminConsultId = chatMapper.getConsultIdSupervisorConsult(fromId, toId);
                    if (adminConsultId != null) {
                        log.debug("找到管理员会话 ({}, {}), ID: {}", fromId, toId, adminConsultId);
                    } else {
                        log.debug("管理员会话 ({}, {}) 也未找到。", fromId, toId);
                    }
                } else {
                    // 第一个方向就找到了
                    log.debug("找到管理员会话 ({}, {}), ID: {}", toId, fromId, adminConsultId);
                    sender = "user";
                }

                // 将最终找到的结果（可能是 null）赋给 consultId
                consultId = adminConsultId;
            } // 与三目运算法逻辑相同
            log.info("向管理员发送消息，consultId为：" + consultId);
            chatMapper.addMessage(consultId, messageFromToDTO.getMessage(), messageFromToDTO.getMessageType(),
                    sender);
        }
        log.info("插入消息数据完成");
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

        // 新增，简易处理admin-admin逻辑，一拖四
        if (messageId == null){
            messageId = chatMapper.getLastMessageIdForAdminAdmin(toId, fromId);
        };
        if (messageId == null){
            messageId = chatMapper.getLastMessageIdForAdminAdmin(fromId, toId);
        };

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

        // 构式逻辑，纯纯的屎山
        if(consultId == null){
            consultId = chatMapper.getConsultIdSupervisorConsult(toId, fromId);
        }
        if(consultId == null){
            consultId = chatMapper.getConsultIdSupervisorConsult(fromId, toId);
        }
        return consultId;
    }

}
