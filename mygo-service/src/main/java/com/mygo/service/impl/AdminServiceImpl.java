package com.mygo.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.constant.ErrorMessage;
import com.mygo.constant.RedisConstant;
import com.mygo.dto.AdminLoginDTO;
import com.mygo.dto.AdminRegisterDTO;
import com.mygo.dto.LastMessageAndTime;
import com.mygo.dto.ResetPasswordDTO;
import com.mygo.entity.Admin;
import com.mygo.entity.Consult;
import com.mygo.entity.Message;
import com.mygo.entity.User;
import com.mygo.enumeration.Sender;
import com.mygo.exception.BadRequestException;
import com.mygo.mapper.AdminMapper;
import com.mygo.mapper.ChatMapper;
import com.mygo.mapper.UserMapper;
import com.mygo.service.AdminService;
import com.mygo.utils.*;
import com.mygo.vo.AdminMessageVO;
import com.mygo.vo.AdminInfoVO;
import com.mygo.vo.AdminLoginVO;
import com.mygo.vo.AdminSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AdminServiceImpl implements AdminService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AdminMapper adminMapper;

    private final JwtTool jwtTool;

    private final StringRedisTemplate stringRedisTemplate;

    private final MailUtils mailUtils;

    private final IdTool idTool;

    private final UserMapper userMapper;

    private final ChatMapper chatMapper;

    @Autowired
    public AdminServiceImpl(AdminMapper adminMapper, JwtTool jwtTool, StringRedisTemplate stringRedisTemplate,
                            MailUtils mailUtils, IdTool idTool, UserMapper userMapper, ChatMapper chatMapper) {
        this.adminMapper = adminMapper;
        this.jwtTool = jwtTool;
        this.stringRedisTemplate = stringRedisTemplate;
        this.mailUtils = mailUtils;
        this.idTool = idTool;
        this.userMapper = userMapper;
        this.chatMapper = chatMapper;
    }

    /**
     * 登陆
     *
     * @param adminLoginDTO 登陆DTO，包括如下字段：<br>
     *                      用户名、密码
     * @return jwt令牌、用户类型
     */
    @Override
    public AdminLoginVO login(AdminLoginDTO adminLoginDTO) {
        log.info("登录服务");
        //1.根据用户名查找是否存在该用户
        Admin admin = adminMapper.getAdminByName(adminLoginDTO.getName());
        if (admin == null) {
            throw new BadRequestException(ErrorMessage.USER_NOT_FOUND);
        }
        //2.判断密码是否正确
        if (!PasswordEncoder.matches(admin.getPassword(), adminLoginDTO.getPassword())) {
            throw new BadRequestException(ErrorMessage.PASSWORD_ERROR);
        }
        log.info(String.valueOf(admin.toString()));
        //3.生成JWT令牌
        String jwt = jwtTool.createJWT(admin.getAdminId());
        //4.将JWT保存在redis中
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.JWT_KEY + admin.getAdminId(), RedisConstant.JWT_VALUE);
        //5.设置过期时间
        stringRedisTemplate.expire(
                RedisConstant.JWT_KEY + admin.getAdminId(), RedisConstant.JWT_EXPIRE, RedisConstant.JWT_EXPIRE_UNIT);
        log.info("redis设置成功");
        //6.返回token
        return new AdminLoginVO(jwt, admin.getRole());
    }

    /**
     * 注册用户
     * @param adminRegisterDTO 注册DTO，包括如下字段：<br>
     *                         用户名、密码、邮箱、身份
     */
    @Override
    public void register(AdminRegisterDTO adminRegisterDTO) throws JsonProcessingException {
        adminMapper.addAdmin(idTool.getPersonId(), adminRegisterDTO.getUsername(), adminRegisterDTO.getName(),
                adminRegisterDTO.getEmail(), PasswordEncoder.encode(adminRegisterDTO.getPassword()),
                adminRegisterDTO.getRole(), objectMapper.writeValueAsString(adminRegisterDTO.getProfile()));
    }

    /**
     * 发送邮箱验证码
     * @param name 用户名
     * @return 用户邮箱
     */
    @Override
    public String sendEmail(String name) {
        //1.通过用户名查询邮件
        String email = adminMapper.getEmailByAccountName(name);
        if (email == null) {
            throw new BadRequestException(ErrorMessage.USER_NOT_FOUND);
        }
        //2.生成6为随机数字
        String num = RandomUtil.randomNumbers(6);
        //3.发送邮件
        String subject = "找回密码验证码";
        String text = "你的验证码为：" + num;
        mailUtils.sendMail(email, subject, text);
        //4.把数据存在redis中
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.ADMIN_VERIFY_KEY +
                        name, num, RedisConstant.VERIFY_EXPIRE, RedisConstant.VERIFY_EXPIRE_UNIT);
        //5.返回邮箱（用于前端展示）
        return email;
    }

    /**
     * 检查用户发送的邮箱验证码。如果正确，重置密码。
     * @param resetPasswordDTO 重置密码DTO，包括如下字段：<br>
     *                         用户名，验证码，密码
     */
    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        String name = resetPasswordDTO.getName();
        //1.从redis读取验证码
        String verificationCode = stringRedisTemplate.opsForValue()
                .get(RedisConstant.ADMIN_VERIFY_KEY + name);
        //2.判断验证码是否正确
        if (!Objects.equals(verificationCode, resetPasswordDTO.getVerifyCode())) {
            throw new BadRequestException(ErrorMessage.VERIFY_CODE_ERROR);
        }
        //3.修改密码
        adminMapper.updatePassword(resetPasswordDTO.getName(), PasswordEncoder.encode(resetPasswordDTO.getPassword()));
    }

    @Override
    public AdminInfoVO getAdminInfo() {
        Integer id = Context.getId();
        Admin admin = adminMapper.getAdminById(id);
        return AdminInfoVO.builder()
                .id(admin.getAdminId()
                        .toString())
                .name(admin.getRealName())
                .username(admin.getAccountName())
                .email(admin.getEmail())
                .role(admin.getRole())
                .createdAt(admin.getCreatedAt())
                .build();
    }

    @Override
    public List<AdminSessionVO> getSession() {
        //1.查询相关consultId和UserId
        Integer id = Context.getId();
        List<Consult> consults = adminMapper.getConsultInfoByAdminId(id);
        List<AdminSessionVO> sessions = new ArrayList<>();
        //2.构建VO
        for (Consult consult : consults) {
            AdminSessionVO adminSessionVO = AdminSessionVO.builder()
                    .id(consult.getConsultId()
                            .toString())
                    .clientId(consult.getUserId()
                            .toString())
                    .counselorId(id.toString())
                    .build();
            User user = userMapper.selectUserById(consult.getUserId());
            adminSessionVO.setClientAvatar(user.getAvatar());
            adminSessionVO.setClientName(user.getName()
                    .isBlank() ? user.getEmail() : user.getName());
            adminSessionVO.setStatus(user.getStatus());
            Admin admin = adminMapper.getAdminById(id);
            adminSessionVO.setClientAvatar(admin.getAvatar());
            adminSessionVO.setClientName(admin.getAccountName());
            LastMessageAndTime lastMessageAndTime = chatMapper.getLastMessage(consult.getConsultId());
            adminSessionVO.setLastMessage(lastMessageAndTime.getMessage());
            adminSessionVO.setLastMessageTime(lastMessageAndTime.getTime());
        }
        return sessions;
    }

    @Override
    public List<AdminMessageVO> getMessages(Integer sessionId, Integer limit, Integer offset) throws JsonProcessingException {
        List<Message> messages = adminMapper.getHistoryMessageBySessionId(sessionId, limit, offset);
        List<AdminMessageVO> messageVOs = new ArrayList<>();
        for (Message message : messages) {
            AdminMessageVO messageVO = AdminMessageVO.builder()
                    .sessionId(sessionId.toString())
                    .type(message.getMessageType())
                    .meta(objectMapper.readValue(message.getMeta(), new TypeReference<>() {
                    }))
                    .content(message.getMessage())
                    .timestamp(message.getTime())
                    .status(message.getStatus())
                    .id(message.getId()
                            .toString())
                    .build();
            Consult consult = chatMapper.getConsultById(sessionId);
            if (message.getSender() == Sender.Admin) {
                messageVO.setSenderId(consult.getAdminId()
                        .toString());
                messageVO.setReceiverId(consult.getUserId()
                        .toString());
            } else if (message.getSender() == Sender.User) {
                messageVO.setSenderId(consult.getUserId()
                        .toString());
                messageVO.setReceiverId(consult.getAdminId()
                        .toString());
            }
            messageVOs.add(messageVO);
        }
        return messageVOs;
    }

}
