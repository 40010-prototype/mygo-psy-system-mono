package com.mygo.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.mygo.config.WebSocketConfig;
import com.mygo.dto.AdminMessageDTO;
import com.mygo.dto.MessageDTO;
import com.mygo.dto.MessageFromToDTO;
import com.mygo.enumeration.MessageStatus;
import com.mygo.mapper.ChatMapper;
import com.mygo.service.ChatService;
import com.mygo.utils.Context;
import com.mygo.utils.JwtTool;
import com.mygo.vo.AdminMessageVO;
import com.mygo.vo.UserMessageVO;
import jakarta.annotation.PostConstruct;
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
@ServerEndpoint("/ws/{device}/{token}")
// @ServerEndpoint(value = "/ws/{device}", configurator = WebSocketConfig.class)
public class WebSocketServer {

    // 建立一个双向哈希表
    private static final Map<String, Session> idToSessionMap = new HashMap<>();

    private static final Map<Session, String> sessionToIdMap = new HashMap<>();

    // 这里必须要加static属性，因为接下来要注入的bean都是单例的，但是webSocketServer这个bean是多例的。

    private static final ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();

        // 注册 JavaTimeModule 以支持 Java 8 日期时间类（如 LocalDateTime）
        objectMapper.registerModule(new JavaTimeModule());

        // 禁用时间戳输出，使用 ISO-8601 格式的日期时间
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // 静态字段用于在WebSocket事件方法中访问
    private static ChatService staticChatService;
    private static JwtTool staticJwtTool;
    private static ChatMapper staticChatMapper;

    // 非静态字段用于Spring注入
    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtTool jwtTool;

    @Autowired
    private ChatMapper chatMapper;

    @PostConstruct
    public void init() {
        // 将实例字段的引用赋值给对应的静态字段
        staticChatService = this.chatService;
        staticJwtTool = this.jwtTool;
        staticChatMapper = this.chatMapper;
        log.info("WebSocketServer依赖注入成功: chatService={}, jwtTool={}, chatMapper={}",
                staticChatService != null ? "注入成功" : "注入失败",
                staticJwtTool != null ? "注入成功" : "注入失败",
                staticChatMapper != null ? "注入成功" : "注入失败");
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("device") String device, @PathParam("token") String token)
            throws IOException {
        // 将请求头中的用户id和请求路径中的管理端/用户端标识拼接，并和Session一起存入双向哈希表中
        if (!Objects.equals(device, "admin") && !Objects.equals(device, "user"))
            session.close();
        System.out.println(token);

        // 使用静态字段
        if (staticJwtTool == null) {
            log.error("staticJwtTool为null，无法处理WebSocket连接");
            session.close();
            return;
        }

        Integer id = staticJwtTool.parseJWT(token);
        System.out.println(id);
        String key = device + "_" + id;
        idToSessionMap.put(key, session);
        sessionToIdMap.put(session, key);
        System.out.println(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("zhe" + throwable.getMessage());
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) { // <--- 增加了 CloseReason 参数
        String key = sessionToIdMap.get(session); // 尝试获取 Key

        // 记录关闭事件 (最好在移除映射之前记录，确保能获取到 key)
        if (key != null) {
            log.info("WebSocket 连接关闭: Key='{}', SessionID='{}', 关闭代码={}, 原因='{}'",
                    key,
                    session.getId(),
                    closeReason.getCloseCode().getCode(), // 获取标准的关闭代码 (数字)
                    closeReason.getReasonPhrase()); // 获取关闭原因短语 (字符串)
        } else {
            // 如果 key 为 null，可能是 session 未成功注册或已被移除
            log.warn("WebSocket 连接关闭，但无法在 sessionToIdMap 中找到对应的 Key。SessionID='{}', 关闭代码={}, 原因='{}'",
                    session.getId(),
                    closeReason.getCloseCode().getCode(),
                    closeReason.getReasonPhrase());
        }

        // 执行清理逻辑 (移除映射)
        if (key != null) {
            idToSessionMap.remove(key); // 从 id->session 映射中移除
        }
        sessionToIdMap.remove(session); // 必须从 session->id 映射中移除 session

        if (closeReason.getCloseCode().getCode() != CloseReason.CloseCodes.NORMAL_CLOSURE.getCode()) {
            // 1000 是正常关闭代码
            log.warn("检测到非正常 WebSocket 关闭: Key='{}', SessionID='{}', Code={}, Reason='{}'",
                    key != null ? key : "[未知Key]", // 处理 key 可能为 null 的情况
                    session.getId(),
                    closeReason.getCloseCode().getCode(),
                    closeReason.getReasonPhrase());
        }
    }

    @OnMessage
    public void onMessage(String jsonMessage, Session session) throws JsonProcessingException {
        // 检查依赖是否已经正确注入
        if (staticChatService == null || staticChatMapper == null) {
            log.error("依赖注入失败: chatService={}, chatMapper={}",
                    staticChatService != null ? "可用" : "不可用",
                    staticChatMapper != null ? "可用" : "不可用");
            return;
        }

        /*
         * 1.根据Session查询发送方id
         * 这里为什么不让发送者直接发一个id过来？
         * 因为这样同样不知道发送者是用户端还是管理端的。
         * 另外一点就是，因为删除的时候要把对象从哈希表中移除，还是要建一个双向哈希表（也就是两个哈希表）
         * 所以利用这个哈希表根据session查一下id也是顺手的事。
         * 不过后续业务会拓展，督导可以和咨询师聊天、督导也可以和用户聊天。
         * 我现在想到了两个解决方案：
         * 1)让前端把发送者、接受者、发送方向一起传过来。（要传就干脆一起传过来）
         * 2)每次都根据接受者id查一下哈希表。这样要查两次，分别是admin_{id)和user_{id},但因为查询本来就是平均O(1)的，也不会很麻烦。
         */

        String key = sessionToIdMap.get(session);
        String fromDevice = Objects.equals(key.split("_")[0], "user") ? "user" : "admin";

        log.info(jsonMessage);
        MessageDTO messageDTO;
        if (fromDevice.equals("user")) {
            // 如果是移动端发送的数据
            messageDTO = objectMapper.readValue(jsonMessage, MessageDTO.class);
            log.info(messageDTO.toString());
        } else {
            AdminMessageDTO adminMessageDTO = objectMapper.readValue(jsonMessage, AdminMessageDTO.class);
            messageDTO = new MessageDTO();
            messageDTO.setMessage(adminMessageDTO.getContent());
            messageDTO.setMessageType(adminMessageDTO.getType());
            messageDTO.setMeta(adminMessageDTO.getMeta());
            messageDTO.setToId(Integer.valueOf(adminMessageDTO.getReceiverId()));
        }
        Integer toId = messageDTO.getToId();
        // Integer toRole = staticChatMapper.getRole(toId);

        // 新增逻辑，构造可能的 Key，然后查询两次哈希。如果两次均未命中，则根据数据库角色判断类型。
        String potentialUserKey = "user_" + toId;
        String potentialAdminKey = "admin_" + toId;
        String toKey = null; // 存储最终确定的接收者Key ("user_xxx" 或 "admin_xxx")

        String toDevice;

        if (idToSessionMap.containsKey(potentialUserKey)) {
            toDevice = "user"; // 确定类型为 user
            toKey = potentialUserKey; // 确定 Key 为 user_ID
            log.info("接收者 ID {} 作为 User 在线", toId);
        } else if (idToSessionMap.containsKey(potentialAdminKey)) {
            toDevice = "admin"; // 确定类型为 admin
            toKey = potentialAdminKey; // 确定 Key 为 admin_ID
            log.info("接收者 ID {} 作为 Admin 在线", toId);
        } else {
            Integer toRole = staticChatMapper.getRole(toId); // 回退查询数据库获取其角色
            if (toRole == null) { // 根据之前的约定，role 为 null 代表是普通用户
                toDevice = "user";
                toKey = potentialUserKey; // 即使不在线，也构造出它应该有的Key
            } else { // role 不为 null，代表是某种管理员角色 (咨询师/督导)
                toDevice = "admin";
                toKey = potentialAdminKey; // 构造出它应该有的Key
            }
            log.info("接收者 ID {} 不在线，根据数据库角色判断类型为: {}", toId, toDevice);
        }

        if (toDevice == null || toKey == null) {
            log.error("无法确定接收者 ID {} 的类型，或ID无效。消息无法处理。", toId);
            // 可以选择向发送者发送错误通知
            // sendErrorToSender(session, "Invalid or unknown recipient ID.");
            return; // 必须终止处理，否则下面构造 DTO 会 NPE
        }

        // if(toRole==null){
        // //是咨询师和用户的聊天
        // toDevice="user";
        // }
        // else{
        // toDevice="admin";
        // }
        MessageFromToDTO messageFromToDTO = new MessageFromToDTO(key, toKey, messageDTO.getMessageType(),
                messageDTO.getMessage(), messageDTO.getMeta(),
                LocalDateTime.now());
        log.info("messageFromToDTO: " + messageFromToDTO.toString());
        // 2.在数据库中插入数据
        staticChatService.receiveMessage(messageFromToDTO);

        // 3.转发消息。这步不放在chatService中是因为会造成循环依赖。
        sendMessage(messageFromToDTO);
    }

    // 双发消息
    public void sendMessage(MessageFromToDTO messageFromToDTO) throws JsonProcessingException {
        try {
            // 检查依赖是否已经正确注入
            if (staticChatService == null) {
                log.error("依赖注入失败: chatService不可用");
                return;
            }

            log.info("开始转发消息");
            log.info(messageFromToDTO.toString());

            boolean toIsUser = Objects.equals(messageFromToDTO.getToId().split("_")[0], "user");
            boolean fromIdUser = Objects.equals(messageFromToDTO.getFromId().split("_")[0], "user");

            // --- 分支 1: 处理用户与管理员之间的消息 ---
            if (toIsUser || fromIdUser) {
                Session userSession;
                Session adminSession;
                if (toIsUser) {
                    userSession = idToSessionMap.get(messageFromToDTO.getToId());
                    adminSession = idToSessionMap.get(messageFromToDTO.getFromId());
                } else {
                    adminSession = idToSessionMap.get(messageFromToDTO.getToId());
                    userSession = idToSessionMap.get(messageFromToDTO.getFromId());
                }
                // 处理移动端逻辑
                if (userSession != null) {
                    UserMessageVO userMessageVo = new UserMessageVO(Integer.valueOf(messageFromToDTO.getFromId()
                            .split("_")[1]), Integer.valueOf(
                                    messageFromToDTO.getToId()
                                            .split("_")[1]),
                            messageFromToDTO.getMessageType(), messageFromToDTO.getMessage(),
                            messageFromToDTO.getMeta(), messageFromToDTO.getTime());
                    log.info("userMessageVo: " + userMessageVo.toString());
                    String text = objectMapper.writeValueAsString(userMessageVo);

                    log.info("移动端消息：" + text);
                    userSession.getAsyncRemote()
                            .sendText(text);
                }
                // 处理网页端逻辑
                if (adminSession != null) {
                    AdminMessageVO adminMessageVO = AdminMessageVO.builder()
                            .content(messageFromToDTO.getMessage())
                            .meta(messageFromToDTO.getMeta())
                            .type(messageFromToDTO.getMessageType())
                            .status(MessageStatus.READ)
                            .receiverId(messageFromToDTO.getToId()
                                    .split("_")[1])
                            .senderId(messageFromToDTO.getFromId()
                                    .split("_")[1])
                            .timestamp(messageFromToDTO.getTime())
                            .build();
                    adminMessageVO.setId(staticChatService.getMessageId(messageFromToDTO)
                            .toString());
                    adminMessageVO.setSessionId(staticChatService.getConsultId(messageFromToDTO)
                            .toString());
                    adminSession.getAsyncRemote()
                            .sendText(objectMapper.writeValueAsString(adminMessageVO));
                }

            } else { // 2. 处理发送给管理端(网页端)的逻辑
                Session adminToSession = idToSessionMap.get(messageFromToDTO.getToId());
                Session adminFromSession = idToSessionMap.get(messageFromToDTO.getFromId());
                // to逻辑

                AdminMessageVO adminMessageVO = AdminMessageVO.builder()
                        .content(messageFromToDTO.getMessage())
                        .meta(messageFromToDTO.getMeta())
                        .type(messageFromToDTO.getMessageType())
                        .status(MessageStatus.READ)
                        .receiverId(messageFromToDTO.getToId()
                                .split("_")[1])
                        .senderId(messageFromToDTO.getFromId()
                                .split("_")[1])
                        .timestamp(messageFromToDTO.getTime())
                        .build();
                adminMessageVO.setId(staticChatService.getMessageId(messageFromToDTO)
                        .toString());
                adminMessageVO.setSessionId(staticChatService.getConsultId(messageFromToDTO)
                        .toString());
                if (adminToSession != null) {
                    adminToSession.getAsyncRemote()
                            .sendText(objectMapper.writeValueAsString(adminMessageVO));
                }

                // from逻辑
                if (adminFromSession != null) {
                    adminFromSession.getAsyncRemote()
                            .sendText(objectMapper.writeValueAsString(adminMessageVO));
                }

            }
        } catch (Exception e) {
            log.info(e.toString());
        }
    }
}
