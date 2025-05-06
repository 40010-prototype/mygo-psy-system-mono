package com.mygo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.constant.RedisConstant;
import com.mygo.dto.*;
import com.mygo.entity.Consult;
import com.mygo.entity.Message;
import com.mygo.entity.User;
import com.mygo.enumeration.MessageType;
import com.mygo.enumeration.Sender;
import com.mygo.exception.BadRequestException;
import com.mygo.mapper.AdminMapper;
import com.mygo.mapper.ChatMapper;
import com.mygo.mapper.UserMapper;
import com.mygo.service.ChatService;
import com.mygo.service.UserService;
import com.mygo.utils.Context;
import com.mygo.utils.IdTool;
import com.mygo.utils.JwtTool;
import com.mygo.utils.PasswordEncoder;
import com.mygo.vo.ActiveCounselorVO;
import com.mygo.vo.AdminMessageVO;
import com.mygo.vo.UserLoginVO;
import com.mygo.vo.UserMessageVO;
import com.mygo.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final JwtTool jwtTool;

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final IdTool idTool;

    private final AdminMapper adminMapper;

    private final ChatMapper chatMapper;

    private final WebSocketServer webSocketServer;

    private final ChatService chatService;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, JwtTool jwtTool, StringRedisTemplate stringRedisTemplate,
                           IdTool idTool, AdminMapper adminMapper, ChatMapper chatMapper,
                           WebSocketServer webSocketServer, ChatService chatService) {
        this.userMapper = userMapper;
        this.jwtTool = jwtTool;
        this.stringRedisTemplate = stringRedisTemplate;
        this.idTool = idTool;
        this.adminMapper = adminMapper;
        this.chatMapper = chatMapper;
        this.webSocketServer = webSocketServer;
        this.chatService = chatService;
    }

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        String email = userRegisterDTO.getEmail();
        String password = userRegisterDTO.getPassword();
        userMapper.addUser(idTool.getPersonId(), email, PasswordEncoder.encode(password));
    }

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        //1.根据用户名查找是否存在该用户
        User user = userMapper.selectUserByEmail(userLoginDTO.getEmail());
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        //2.判断密码是否正确
        if (!PasswordEncoder.matches(user.getPassword(), userLoginDTO.getPassword())) {
            throw new BadRequestException("密码错误");
        }
        //3.生成JWT令牌
        String jwt = jwtTool.createJWT(user.getUserId());
        //4.将JWT保存在redis中
        //这里不使用hash,因为要分别设置过期时间
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.JWT_KEY + user.getUserId(), RedisConstant.JWT_VALUE);
        //5.设置过期时间
        stringRedisTemplate.expire(
                RedisConstant.JWT_KEY + user.getUserId(), RedisConstant.JWT_EXPIRE, RedisConstant.JWT_EXPIRE_UNIT);
        //6.返回token
        return new UserLoginVO(jwt, true);
    }

    @Override
    public void addInfo(UserAddInfoDTO userAddInfoDTO) {
        userMapper.updateInfo(userAddInfoDTO.getName(),userAddInfoDTO.getPhone(), userAddInfoDTO.getGender(), userAddInfoDTO.getAge(),
                userAddInfoDTO.getEmergencyContact(), userAddInfoDTO.getEmergencyContactPhone(),Context.getId());
    }

    @Override
    public List<ActiveCounselorVO> getActiveCounselor() {
        LocalDate currentDate = LocalDate.now();  // 当前日期：YYYY-MM-DD

        // 将 LocalDate 转换为 java.util.Date，且时间为 00:00:00
        Date date = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

// 获取当前时间（精确到 HH:mm:ss，去除毫秒）
        LocalTime currentTime = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);  // 去除毫秒部分
        List<Integer> activeCounselors = userMapper.getActiveCounselor(date, currentTime);
        List<ActiveCounselorVO> activeCounselorVOS = new ArrayList<>();
        for (Integer activeCounselorId : activeCounselors) {
            ActiveCounselorVO activeCounselorVO = new ActiveCounselorVO();
            activeCounselorVO.setId(activeCounselorId);
            activeCounselorVO.setName(adminMapper.getNameById(activeCounselorId));
            activeCounselorVOS.add(activeCounselorVO);
        }
        return activeCounselorVOS;

    }

    @Override
    public void setSession(Integer counselorId) throws JsonProcessingException {
        Integer userId = Context.getId();
        Integer consult = chatMapper.getConsult(userId, counselorId);
        if (consult != null) {
            return;
        }
        chatMapper.addConsult(userId, counselorId);
        MessageFromToDTO messageFromToDTO = new MessageFromToDTO(
                "user_" + userId.toString(), "admin_" +
                counselorId.toString(), MessageType.TEXT, "您好，向您咨询一些心理问题", null, LocalDateTime.now());
        chatService.receiveMessage(messageFromToDTO);
        webSocketServer.sendMessage(messageFromToDTO);
    }

    @Override
    public List<UserMessageVO> getMessages(Integer counselorId) throws JsonProcessingException { // 注意：如果代码中不再有操作抛出
                                                                                                 // JsonProcessingException，可以移除
                                                                                                 // throws 声明
        Integer userId = Context.getId();
        Integer sessionId = chatMapper.getConsult(userId, counselorId);

        // 检查 sessionId 是否有效，避免后续空指针等问题 (可选但推荐)
        if (sessionId == null) {
            // 可以返回空列表或抛出异常，取决于业务逻辑
            return new ArrayList<>();
        }

        List<Message> messages = adminMapper.getHistoryMessageBySessionId(sessionId, 999, 0);
        List<UserMessageVO> messageVOs = new ArrayList<>();

        for (Message message : messages) {
            Integer senderId;
            Integer receiverId;

            if (message.getSender() == Sender.User) {
                senderId = userId;
                receiverId = counselorId;
            } else {
                senderId = counselorId;
                receiverId = userId;
            }

            try {
                // 1. 使用 builder 配置
                // 2. 调用 .build() 创建对象
                UserMessageVO messageVO = UserMessageVO.builder()
                        .fromId(senderId)
                        .toId(receiverId)
                        .messageType(message.getMessageType())
                        .message(message.getMessage())
                        // 可能还需要设置其他 messageVO 的属性，比如时间戳等
                        .build(); // <-- 关键：调用 build() 创建对象

                // 3. 将创建的对象添加到 list 中
                messageVOs.add(messageVO); // <-- 关键：添加到列表

            } catch (Exception e) {
                // 这里的异常处理需要根据实际情况调整
                // 如果 builder().build() 可能因为数据问题失败，需要记录日志或采取其他措施
                // 直接抛出 BadRequestException 可能不合适，除非明确是客户端请求数据导致的问题
                // 例如，可以记录错误日志并跳过这条消息：
                System.err.println("Error processing message: " + message.getId() + ", error: " + e.getMessage());
                // 或者根据业务需求决定是否抛出异常中断整个过程
                // throw new RuntimeException("Failed to process message " + message.getId(),
                // e);
            }
        } // 结束 for 循环

        return messageVOs; // 返回包含转换后对象的列表
    }

    @Override
    public void endSession(EndSessionDTO endSessionDTO) {
        Integer userId = Context.getId();
        Integer counselorId=endSessionDTO.getCounselorId();
        Integer score = endSessionDTO.getScore();
        userMapper.endSession(userId,counselorId,score);
    }

}
