package com.mygo.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.config.WebSocketConfig;
import com.mygo.mapper.AdminMapper;
import com.mygo.mapper.UserMapper;
import com.mygo.service.ChatService;
import com.mygo.utils.Context;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
//@ServerEndpoint("/chat/{device}")
@ServerEndpoint(value = "/ws/{device}",configurator = WebSocketConfig.class)
public class WebSocketServer {

    private static final Map<String, Session> sessionMap = new HashMap<>();


    //这里必须要加static属性，因为接下来要注入的bean都是单例的，但是webSocketServer这个bean是多例的。
    private static AdminMapper adminMapper;

    private static UserMapper userMapper;

    private static ChatService chatService;

    @Autowired
    public void startWebSocketServer(AdminMapper adminMapper,UserMapper userMapper,ChatService chatService) {
        WebSocketServer.adminMapper=adminMapper;
        WebSocketServer.userMapper = userMapper;
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
    public void onOpen(Session session, @PathParam("device") String device) {

        Long id = Context.getId();
        String key = device + "_" + id;
        sessionMap.put(key, session);
        System.out.println("onOpen");
        System.out.println(Context.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessionMap.remove(session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) throws JsonProcessingException {
        sendToAllClient(message);
        chatService.receiveMessage(message);
    }

    public void sendToAllClient(String message) {
        for (Session session : sessionMap.values()) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote()
                        .sendText(message);
            } catch (Exception e) {
                log.error("向客户端发送消息失败", e);
            }
        }
    }
}
