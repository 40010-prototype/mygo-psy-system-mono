package com.mygo.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.config.WebSocketConfig;
import com.mygo.dto.MessageDTO;
import com.mygo.dto.MessageFromToDTO;
import com.mygo.service.ChatService;
import com.mygo.utils.Context;
import com.mygo.vo.AdminMessageVO;
import com.mygo.vo.UserMessageVO;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
//@ServerEndpoint("/chat/{device}")
@ServerEndpoint(value = "/ws/{device}", configurator = WebSocketConfig.class)
public class WebSocketServer {

    //建立一个双向哈希表
    private static final Map<String, Session> idToSessionMap = new HashMap<>();

    private static final Map<Session, String> sessionToIdMap = new HashMap<>();

    //这里必须要加static属性，因为接下来要注入的bean都是单例的，但是webSocketServer这个bean是多例的。
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static ChatService chatService;

    @Autowired
    public void setWebSocketServer(ChatService chatService) {
        WebSocketServer.chatService = chatService;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("device") String device) throws IOException {
        //将请求头中的用户id和请求路径中的管理端/用户端标识拼接，并和Session一起存入双向哈希表中
        if (!Objects.equals(device, "admin") && !Objects.equals(device, "user")) session.close();
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
        /*1.根据Session查询发送方id
         * 这里为什么不让发送者直接发一个id过来？
         * 因为这样同样不知道发送者是用户端还是管理端的。
         * 另外一点就是，因为删除的时候要把对象从哈希表中移除，还是要建一个双向哈希表（也就是两个哈希表）
         * 所以利用这个哈希表根据session查一下id也是顺手的事。
         * 不过后续业务会拓展，督导可以和咨询师聊天、督导也可以和用户聊天。
         * 我现在想到了两个解决方案：
         * 1)让前端把发送者、接受者、发送方向一起传过来。（要传就干脆一起传过来）
         * 2)每次都根据接受者id查一下哈希表。这样要查两次，分别是admin_{id)和user_{id},但因为查询本来就是平均O(1)的，也不会很麻烦。
         */
        MessageDTO messageDTO = objectMapper.readValue(jsonMessage, MessageDTO.class);
        String key = sessionToIdMap.get(session);
        String toDevice = Objects.equals(key.split("_")[0], "admin") ? "admin" : "user";
        MessageFromToDTO messageFromToDTO = new MessageFromToDTO(key, toDevice +
                messageDTO.getToId(), messageDTO.getMessageType(), messageDTO.getMessage(), messageDTO.getMeta(),
                LocalDateTime.now());
        //2.在数据库中插入数据
        chatService.receiveMessage(messageFromToDTO);
        //3.转发消息。这步不放在chatService中是因为会造成循环依赖。
        sendMessage(messageFromToDTO);
    }

    //双发消息
    public void sendMessage(MessageFromToDTO messageFromToDTO) throws JsonProcessingException {
        boolean isToUser = messageFromToDTO.getToId()
                .split("_")[0].equals("user");
        Session userSession;
        Session adminSession;
        if (isToUser) {
            userSession = idToSessionMap.get(messageFromToDTO.getToId());
            adminSession = idToSessionMap.get(messageFromToDTO.getFromId());
        } else {
            adminSession = idToSessionMap.get(messageFromToDTO.getToId());
            userSession = idToSessionMap.get(messageFromToDTO.getFromId());
        }
        if (userSession == null || adminSession == null) return;
        //处理移动端逻辑
        UserMessageVO userMessageVo = new UserMessageVO(Integer.valueOf(messageFromToDTO.getFromId()
                .split("_")[1]), Integer.valueOf(messageFromToDTO.getToId()
                .split("_")[1]), messageFromToDTO.getMessageType(), messageFromToDTO.getMessage(),
                messageFromToDTO.getMeta(), messageFromToDTO.getTime());
        String text = objectMapper.writeValueAsString(userMessageVo);
        userSession.getAsyncRemote()
                .sendText(text);
        //处理网页端逻辑
        AdminMessageVO adminMessageVO = AdminMessageVO.builder()
                .content(messageFromToDTO.getMessage())
                .meta(messageFromToDTO.getMeta())
                .receiverId(messageFromToDTO.getToId()
                        .split("_")[0])
                .senderId(messageFromToDTO.getFromId()
                        .split("_")[0])
                .timestamp(messageFromToDTO.getTime())
                .build();
        adminMessageVO.setId(chatService.getMessageId(messageFromToDTO)
                .toString());
        adminMessageVO.setSessionId(chatService.getConsultId(messageFromToDTO)
                .toString());
        adminSession.getAsyncRemote()
                .sendText(text);
    }

}
