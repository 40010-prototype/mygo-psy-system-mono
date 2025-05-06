package com.mygo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.constant.RedisConstant;
import com.mygo.dto.MessageFromToDTO;
import com.mygo.dto.UserAddInfoDTO;
import com.mygo.dto.UserLoginDTO;
import com.mygo.dto.UserRegisterDTO;
import com.mygo.entity.User;
import com.mygo.enumeration.MessageType;
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
import com.mygo.vo.UserLoginVO;
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

}
