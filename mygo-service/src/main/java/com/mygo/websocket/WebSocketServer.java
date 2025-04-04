package com.mygo.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.config.WebSocketConfig;
import com.mygo.domain.dto.MessageDTO;
import com.mygo.domain.entity.Message;
import com.mygo.domain.vo.MessageVO;
import com.mygo.mapper.AdminMapper;
import com.mygo.mapper.UserMapper;
import com.mygo.service.ChatService;
import com.mygo.utils.Context;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
//@ServerEndpoint("/chat/{device}")
@ServerEndpoint(value = "/ws/{device}",configurator = WebSocketConfig.class)
public class WebSocketServer {


    //建立一个双向哈希表
    private static final Map<String, Session> idToSessionMap = new HashMap<>();

    private static final Map<Session,String> sessionToIdMap = new HashMap<>();


    //这里必须要加static属性，因为接下来要注入的bean都是单例的，但是webSocketServer这个bean是多例的。


    private static ChatService chatService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setWebSocketServer(AdminMapper adminMapper,UserMapper userMapper,ChatService chatService) {
        WebSocketServer.chatService=chatService;
    }
//

//
//    @Autowired
//    public WebSocketServer(AdminMapper adminMapper, UserMapper userMapper) {
//        this.adminMapper = adminMapper;
//        this.userMapper = userMapper;
////        this.chatService = chatService;
//    }

//    , @PathParam("info") String info
    @OnOpen
    public void onOpen(Session session, @PathParam("device") String device) throws IOException {
        if(!Objects.equals(device, "admin")&&!Objects.equals(device,"user"))
            session.close();
        Integer id = Context.getId();
        String key = device + "_" + id;
        idToSessionMap.put(key, session);
        sessionToIdMap.put(session, key);
        System.out.println(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
    }



    @OnClose
    public void onClose(Session session) {
        String key = sessionToIdMap.get(session);
        idToSessionMap.remove(key);
        sessionToIdMap.remove(session);
    }

    @OnMessage
    public void onMessage(String jsonMessage, Session session) throws JsonProcessingException {
        MessageDTO messageDTO = objectMapper.readValue(jsonMessage, MessageDTO.class);
        String key = sessionToIdMap.get(session);
        String toDevice= Objects.equals(key.split("_")[0], "admin") ?"admin":"user";
        Message message = new Message(key,toDevice+messageDTO.getToId(),messageDTO.getMessageType(),messageDTO.getMessage());
        chatService.receiveMessage(message);
    }

    public void sendMessage(Message message) throws JsonProcessingException {

        Session session = idToSessionMap.get(message.getToId());
        if (session != null) {
            MessageVO messageVo=new MessageVO(Integer.valueOf(message.getToId()
                    .split("_")[1]),message.getMessageType(), message.getMessage());
            String text=objectMapper.writeValueAsString(messageVo);
            session.getAsyncRemote().sendText(text);
        }

    }
}
