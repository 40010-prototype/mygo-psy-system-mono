package com.mygo.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.constant.ErrorMessage;
import com.mygo.constant.RedisConstant;
import com.mygo.dto.*;
import com.mygo.entity.*;
import com.mygo.enumeration.Role;
import com.mygo.enumeration.ScheduleStatus;
import com.mygo.enumeration.Sender;
import com.mygo.enumeration.UserStatus;
import com.mygo.exception.BadRequestException;
import com.mygo.mapper.AdminMapper;
import com.mygo.mapper.ChatMapper;
import com.mygo.mapper.UserMapper;
import com.mygo.service.AdminService;
import com.mygo.utils.*;
import com.mygo.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // @Override
    // public List<AdminSessionVO> getSession() {
    //     log.info("开始执行getSession方法");
    //     //1.查询相关consultId和UserId
    //     Integer id = Context.getId();
    //     log.info("当前用户ID: {}", id);
        
    //     List<Consult> consults = adminMapper.getUserConsultsByAdminId(id);
    //     log.info("获取到的consults数量: {}", consults != null ? consults.size() : 0);
        
    //     // 调试输出consults内容
    //     if (consults != null) {
    //         log.info("--- Consults列表详细信息 ---");
    //         int i = 0;
    //         for (Consult c : consults) {
    //             log.info("Consult #{}: consultId={}, participant1AdminId={}, participant2UserId={}, participant2AdminId={}",
    //                     i++, c.getConsultId(), c.getParticipant1AdminId(), c.getParticipant2UserId(), c.getParticipant2AdminId());
    //         }
    //         log.info("--- Consults列表详细信息结束 ---");
    //     }
        
    //     List<AdminSessionVO> sessions = new ArrayList<>();
    //     //2.构建VO
    //     for (Consult consult : consults) {
    //         log.info("处理会话，consultId: {}", consult.getConsultId());
            
    //         // 获取clientId前先检查相关字段
    //         log.info("consult.getUserId()={}, consult.getAdminId()={}", consult.getUserId(), consult.getAdminId());
    //         log.info("原始数据: participant1AdminId={}, participant2UserId={}, participant2AdminId={}", 
    //                 consult.getParticipant1AdminId(), consult.getParticipant2UserId(), consult.getParticipant2AdminId());
            
    //         Integer clientId = consult.getUserId() == null ? consult.getAdminId() : consult.getUserId();
    //         log.info("计算得到的clientId: {}", clientId);
            
    //         // 空值检查
    //         if (clientId == null) {
    //             log.error("警告: clientId为null! consultId={}", consult.getConsultId());
    //             log.error("跳过此条会话记录处理");
    //             continue; // 跳过此条记录，避免NPE
    //         }
            
    //         AdminSessionVO adminSessionVO = AdminSessionVO.builder()
    //                 .id(consult.getConsultId().toString())
    //                 .clientId(clientId.toString())
    //                 .counselorId(id.toString())
    //                 .build();
            
    //         // 判断会话类型
    //         boolean isUserConsult = (consult.getParticipant2UserId() != null);
    //         log.info("会话类型: {}", isUserConsult ? "用户-咨询师" : "督导-咨询师");
            
    //         if (isUserConsult) {
    //             // 用户-咨询师会话
    //             User user = userMapper.selectUserById(clientId);
    //             log.info("获取到的user: {}", user != null ? "成功" : "null");
    //             if (user != null) {
    //                 adminSessionVO.setClientAvatar(user.getAvatar());
    //                 adminSessionVO.setClientName(user.getName() == null ? user.getEmail() : user.getName());
    //                 adminSessionVO.setStatus(user.getStatus());
    //             } else {
    //                 log.error("警告: 无法找到userId={}的用户信息", clientId);
    //             }
    //         } else {
    //             // 督导-咨询师会话
    //             Admin client = adminMapper.getAdminById(clientId);
    //             log.info("获取到的admin: {}", client != null ? "成功" : "null");
    //             if (client != null) {
    //                 adminSessionVO.setClientAvatar(client.getAvatar());
    //                 adminSessionVO.setClientName(client.getAccountName());
    //                 // 设置一个默认状态
    //                 adminSessionVO.setStatus(UserStatus.ACTIVE);
    //             } else {
    //                 log.error("警告: 无法找到adminId={}的管理员信息", clientId);
    //             }
    //         }
            
    //         Admin admin = adminMapper.getAdminById(id);
    //         log.info("获取当前用户信息: {}", admin != null ? "成功" : "null");
    //         if (admin != null) {
    //             adminSessionVO.setCounselorAvatar(admin.getAvatar());
    //             adminSessionVO.setCounselorName(admin.getAccountName());
    //         }
            
    //         LastMessageAndTime lastMessageAndTime = chatMapper.getLastMessage(consult.getConsultId());
    //         log.info("获取最后消息: {}", lastMessageAndTime != null ? "成功" : "null");
    //         if (lastMessageAndTime != null) {
    //             adminSessionVO.setLastMessage(lastMessageAndTime.getMessage());
    //             adminSessionVO.setLastMessageTime(lastMessageAndTime.getTime());
    //         }
            
    //         sessions.add(adminSessionVO);
    //         log.info("成功添加会话到结果列表");
    //     }
        
    //     log.info("getSession方法执行完成，返回会话数量: {}", sessions.size());
    //     return sessions;
    // }

    // @Override
    // public List<AdminMessageVO> getMessages(Integer sessionId, Integer limit, Integer offset) throws JsonProcessingException {
    //     log.info("开始执行getMessages方法，sessionId={}, limit={}, offset={}", sessionId, limit, offset);
        
    //     List<Message> messages = adminMapper.getHistoryMessageBySessionId(sessionId, limit, offset);
    //     log.info("获取到的消息数量: {}", messages != null ? messages.size() : 0);
        
    //     // 调试输出messages内容
    //     if (messages != null && !messages.isEmpty()) {
    //         log.info("--- 消息列表详细信息 ---");
    //         int i = 0;
    //         for (Message m : messages) {
    //             log.info("Message #{}: id={}, sender={}, messageType={}, time={}", 
    //                 i++, m.getId(), m.getSender(), m.getMessageType(), m.getTime());
    //         }
    //         log.info("--- 消息列表详细信息结束 ---");
    //     }
        
    //     List<AdminMessageVO> messageVOs = new ArrayList<>();
        
    //     // 获取会话信息
    //     Consult consult = null;
    //     if (sessionId != null) {
    //         consult = chatMapper.getConsultById(sessionId);
    //         log.info("获取会话信息: {}", consult != null ? "成功" : "失败");
    //         if (consult != null) {
    //             log.info("会话详情: consultId={}, participant1AdminId={}, participant2UserId={}, participant2AdminId={}",
    //                 consult.getConsultId(), 
    //                 consult.getParticipant1AdminId(), 
    //                 consult.getParticipant2UserId(), 
    //                 consult.getParticipant2AdminId());
    //         } else {
    //             log.error("警告: 无法找到sessionId={}的会话信息", sessionId);
    //         }
    //     } else {
    //         log.error("警告: sessionId为null");
    //     }
        
    //     for (Message message : messages) {
    //         log.info("处理消息id={}, sender={}", message.getId(), message.getSender());
            
    //         try {
    //             AdminMessageVO messageVO = AdminMessageVO.builder()
    //                     .sessionId(sessionId.toString())
    //                     .content(message.getMessage())
    //                     .type(message.getMessageType())
    //                     .meta(message.getMeta() == null ? null : 
    //                         objectMapper.readValue(message.getMeta(), new TypeReference<>() {}))
    //                     .timestamp(message.getTime())
    //                     .status(message.getStatus())
    //                     .id(message.getId().toString())
    //                     .build();
                
    //             // Meta数据处理记录
    //             if (message.getMeta() != null) {
    //                 log.info("消息meta数据: {}", message.getMeta());
    //             }
                
    //             // 设置发送者和接收者
    //             if (consult != null) {
    //                 if (message.getSender() == Sender.Admin) {
    //                     // 如果sender是Admin，则发送者是participant1_admin_id
    //                     Integer senderId = consult.getParticipant1AdminId();
    //                     log.info("Admin发送的消息，设置发送者ID={}", senderId);
    //                     if (senderId != null) {
    //                         messageVO.setSenderId(senderId.toString());
    //                     } else {
    //                         log.error("警告: Admin消息发送者ID为null");
    //                     }
                        
    //                     // 接收者可能是用户或咨询师
    //                     if (consult.getParticipant2UserId() != null) {
    //                         log.info("接收者是用户，ID={}", consult.getParticipant2UserId());
    //                         messageVO.setReceiverId(consult.getParticipant2UserId().toString());
    //                     } else if (consult.getParticipant2AdminId() != null) {
    //                         log.info("接收者是管理员，ID={}", consult.getParticipant2AdminId());
    //                         messageVO.setReceiverId(consult.getParticipant2AdminId().toString());
    //                     } else {
    //                         log.error("警告: Admin消息接收者ID为null");
    //                     }
    //                 } else if (message.getSender() == Sender.User) {
    //                     // 如果sender是User，则根据participant2类型确定发送者
    //                     log.info("User发送的消息，检查participant2类型");
    //                     if (consult.getParticipant2UserId() != null) {
    //                         log.info("发送者是用户，ID={}", consult.getParticipant2UserId());
    //                         messageVO.setSenderId(consult.getParticipant2UserId().toString());
    //                         messageVO.setReceiverId(consult.getParticipant1AdminId().toString());
    //                     } else if (consult.getParticipant2AdminId() != null) {
    //                         log.info("发送者是管理员，ID={}", consult.getParticipant2AdminId());
    //                         messageVO.setSenderId(consult.getParticipant2AdminId().toString());
    //                         messageVO.setReceiverId(consult.getParticipant1AdminId().toString());
    //                     } else {
    //                         log.error("警告: User消息发送者ID为null");
    //                     }
    //                 } else {
    //                     log.warn("未知的发送者类型: {}", message.getSender());
    //                 }
    //             } else {
    //                 log.error("无法设置消息的发送者和接收者，因为会话信息为null");
    //             }
                
    //             messageVOs.add(messageVO);
    //             log.info("成功添加消息到结果列表");
    //         } catch (Exception e) {
    //             log.error("处理消息时发生错误: {}", e.getMessage(), e);
    //             // 继续处理下一条消息
    //         }
    //     }
        
    //     log.info("getMessages方法执行完成，返回消息数量: {}", messageVOs.size());
    //     return messageVOs;
    // }

    public List<AdminSessionVO> getSession() {
        //1.查询相关consultId和UserId
        Integer id = Context.getId();
        List<Consult> consults = adminMapper.getConsultInfoByAdminId(id);
        List<AdminSessionVO> sessions = new ArrayList<>();
        //2.构建VO
        for (Consult consult : consults) {
            log.info(String.valueOf(consult.getParticipant2UserId()));
            AdminSessionVO adminSessionVO = AdminSessionVO.builder()
                    .id(consult.getConsultId()
                            .toString())
                    .clientId(consult.getUserId()
                            .toString())
                    .counselorId(id.toString())
                    .build();
            User user = userMapper.selectUserById(consult.getUserId());
            adminSessionVO.setClientAvatar(user.getAvatar());
            adminSessionVO.setClientName(user.getName() == null ? user.getEmail() : user.getName());
            adminSessionVO.setStatus(user.getStatus());
            Admin admin = adminMapper.getAdminById(id);
            adminSessionVO.setCounselorAvatar(admin.getAvatar());
            adminSessionVO.setCounselorName(admin.getAccountName());
            LastMessageAndTime lastMessageAndTime = chatMapper.getLastMessage(consult.getConsultId());
            if (lastMessageAndTime != null) {
                adminSessionVO.setLastMessage(lastMessageAndTime.getMessage());
                adminSessionVO.setLastMessageTime(lastMessageAndTime.getTime());
            }
            sessions.add(adminSessionVO);
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
                    .content(message.getMessage())
                    .type(message.getMessageType())
                    .meta(message.getMeta() ==
                            null ? null : objectMapper.readValue(message.getMeta(), new TypeReference<>() {
                    }))
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

    @Override
    public void read(Integer sessionId) {
        adminMapper.setRead(sessionId);
    }

    @Override
    public void addSchedule(AdminAddScheduleDTO adminAddScheduleDTO) {
        Integer adminId = Context.getId();
        Integer exist = adminMapper.getCounselorStatus(adminId);
        if (exist == null) {
            adminMapper.addCounselorStatus(adminId, adminAddScheduleDTO.getOverallStatus());
        } else {
            adminMapper.changeCounselorStatus(adminId, adminAddScheduleDTO.getOverallStatus());
        }
        List<Schedule> schedules = adminAddScheduleDTO.getSchedules();
        for (Schedule schedule : schedules) {
            LocalDate date = schedule.getDate();
            List<StartAndEndTime> periods = adminMapper.getTimePeriodByDateAndAdminId(date, adminId);
            System.out.println(periods);
            if (periods == null || periods.isEmpty()) {
                adminMapper.addScheduleStatus(date, adminId);
            }
            List<TimeSlotDTO> timeSlots = schedule.getTimeSlots();
            for (TimeSlotDTO timeSlot : timeSlots) {
                StartAndEndTime insertPeriod = new StartAndEndTime(timeSlot.getStartTime(), timeSlot.getEndTime());
                if (periods != null) {
                    for (StartAndEndTime startAndEndTime : periods) {
                        if (insertPeriod.conflict(startAndEndTime))
                            throw new BadRequestException(ErrorMessage.TIME_CONFLICT);

                    }
                }
                adminMapper.addSchedule(adminId, date, timeSlot.getStartTime(), timeSlot.getEndTime(),
                        timeSlot.getStatus());
            }
        }
    }

    @Override
    public void approveScheduleByDay(Integer scheduleId) {
        adminMapper.approveScheduleByDay(scheduleId);
    }

    @Override
    public void approveScheduleByTimeSlot(Integer timeSlotId) {
        adminMapper.approveScheduleByTimeSlot(timeSlotId);
    }

    @Override
    public AdminScheduleVO getScheduleByCounselor(Date startDate, Date endDate) {
        Integer adminId = Context.getId();
        log.info(adminId.toString());
        return getScheduleByCounselorAux(startDate, endDate, adminId);

    }

    private AdminScheduleVO getScheduleByCounselorAux(Date startDate, Date endDate, Integer adminId) {
        System.out.println("这里的id" + adminId);
        String adminName = adminMapper.getAdminById(adminId)
                .getAccountName();
        AdminScheduleVO adminScheduleVO = new AdminScheduleVO();
        adminScheduleVO.setCounselorName(adminName);
        adminScheduleVO.setId(adminId.toString());
        log.info(adminScheduleVO.toString());
        List<ScheduleAndStatusDTO> schedules = new ArrayList<>();
        List<DateAndStatusDTO> dateAndStatusDTOS = adminMapper.getDateAndStatusBetween(startDate, endDate, adminId);
        for (DateAndStatusDTO dateAndStatus : dateAndStatusDTOS) {
            AddScheduleList(adminId, schedules, dateAndStatus);
        }
        adminScheduleVO.setSchedules(schedules);
        ScheduleStatusDTO counselorStatusById = adminMapper.getCounselorStatusById(adminId);
        adminScheduleVO.setOverallStatus(counselorStatusById == null ? null : counselorStatusById.getScheduleStatus());
        return adminScheduleVO;
    }

    private void AddScheduleList(Integer adminId, List<ScheduleAndStatusDTO> schedules,
                                 DateAndStatusDTO dateAndStatus) {
        ScheduleAndStatusDTO scheduleAndStatusDTO = new ScheduleAndStatusDTO();
        scheduleAndStatusDTO.setDate(dateAndStatus.getDate());
        scheduleAndStatusDTO.setStatus(dateAndStatus.getStatus());
        scheduleAndStatusDTO.setApprovalRemark(dateAndStatus.getApprovalRemark());
        List<TimeSlotDTO> timeSlotVOs = new ArrayList<>();
        List<TimeSlot> timeSlots = adminMapper.getTimeSlotByDateAndAdminId(dateAndStatus.getDate(), adminId);
        for (TimeSlot timeSlot : timeSlots) {
            TimeSlotDTO timeSlotVO = TimeSlotDTO.builder()
                    .startTime(timeSlot.getStartTime())
                    .endTime(timeSlot.getEndTime())
                    .status(timeSlot.getStatus())
                    .approvalStatus(timeSlot.getApprovalStatus())
                    .id(timeSlot.getId()
                            .toString())
                    .remark(timeSlot.getRemark())
                    .build();
            timeSlotVOs.add(timeSlotVO);
        }
        scheduleAndStatusDTO.setTimeSlots(timeSlotVOs);
        schedules.add(scheduleAndStatusDTO);
    }

    private AdminScheduleVO getPendingScheduleByCounselorAux(Date startDate, Date endDate, Integer adminId) {
        String adminName = adminMapper.getAdminById(adminId)
                .getAccountName();
        AdminScheduleVO adminScheduleVO = new AdminScheduleVO();
        adminScheduleVO.setCounselorName(adminName);
        adminScheduleVO.setId(adminId.toString());
        List<ScheduleAndStatusDTO> schedules = new ArrayList<>();
        List<DateAndStatusDTO> dateAndStatusDTOS = adminMapper.getDateAndStatusBetween(startDate, endDate, adminId);
        for (DateAndStatusDTO dateAndStatus : dateAndStatusDTOS) {
            if (dateAndStatus.getStatus() == ScheduleStatus.PENDING) {
                AddScheduleList(adminId, schedules, dateAndStatus);
            } else {
                ScheduleAndStatusDTO scheduleAndStatusDTO = new ScheduleAndStatusDTO();
                scheduleAndStatusDTO.setDate(dateAndStatus.getDate());
                scheduleAndStatusDTO.setStatus(dateAndStatus.getStatus());
                scheduleAndStatusDTO.setApprovalRemark(dateAndStatus.getApprovalRemark());
                List<TimeSlotDTO> timeSlotVOs = new ArrayList<>();
                List<TimeSlot> timeSlots = adminMapper.getTimeSlotByDateAndAdminId(dateAndStatus.getDate(), adminId);
                for (TimeSlot timeSlot : timeSlots) {
                    if (timeSlot.getApprovalStatus() == ScheduleStatus.PENDING) {
                        TimeSlotDTO timeSlotVO = TimeSlotDTO.builder()
                                .startTime(timeSlot.getStartTime())
                                .endTime(timeSlot.getEndTime())
                                .status(timeSlot.getStatus())
                                .approvalStatus(timeSlot.getApprovalStatus())
                                .id(timeSlot.toString())
                                .remark(timeSlot.getRemark())
                                .build();
                        timeSlotVOs.add(timeSlotVO);
                    }

                }
                scheduleAndStatusDTO.setTimeSlots(timeSlotVOs);
                schedules.add(scheduleAndStatusDTO);
            }

        }
        adminScheduleVO.setSchedules(schedules);
        adminScheduleVO.setOverallStatus(adminMapper.getCounselorStatusById(adminId)
                .getScheduleStatus());
        return adminScheduleVO;
    }

    @Override
    public List<AdminScheduleVO> getScheduleBySupervisor(Date startDate, Date endDate) {
        Integer supervisorId = Context.getId();
        List<Integer> counselorIds = adminMapper.getCounselorBySupervisor(supervisorId);
        List<AdminScheduleVO> adminScheduleVOS = new ArrayList<>();
        for (Integer counselorId : counselorIds) {
            adminScheduleVOS.add(getScheduleByCounselorAux(startDate, endDate, counselorId));
        }
        return adminScheduleVOS;
    }

    @Override
    public List<AdminScheduleVO> getScheduleByManager(Date startDate, Date endDate) {
        List<Integer> counselorIds = adminMapper.getAllCounselorId();
        List<AdminScheduleVO> adminScheduleVOS = new ArrayList<>();
        for (Integer counselorId : counselorIds) {
            adminScheduleVOS.add(getScheduleByCounselorAux(startDate, endDate, counselorId));
        }
        return adminScheduleVOS;
    }

    @Override
    public List<AdminScheduleVO> getPendingScheduleBySupervisor(Date startDate, Date endDate) {
        Integer supervisorId = Context.getId();
        List<Integer> counselorIds = adminMapper.getCounselorBySupervisor(supervisorId);
        List<AdminScheduleVO> adminScheduleVOS = new ArrayList<>();
        for (Integer counselorId : counselorIds) {
            adminScheduleVOS.add(getPendingScheduleByCounselorAux(startDate, endDate, counselorId));
        }
        return adminScheduleVOS;
    }

    @Override
    public List<SelectAdminVO> getAllAdminByRole(Role role) {
        List<Admin> allCounselor = adminMapper.getAllAdminByRole(role);
        List<SelectAdminVO> vo = new ArrayList<>();
        for (Admin counselor : allCounselor) {
            SelectAdminVO selectCounselorVO = SelectAdminVO.builder()
                    .id(counselor.getAdminId()
                            .toString())
                    .username(counselor.getAccountName())
                    .name(counselor.getRealName())
                    .phone(counselor.getPhone())
                    .avatar(counselor.getAvatar())
                    .email(counselor.getEmail())
                    .profile(counselor.getInfo())
                    .role(role)
                    .createdAt(counselor.getCreatedAt().toString())
                    .build();
            vo.add(selectCounselorVO);
        }
        return vo;
    }

    @Override
    public HelpVO getHelpSessionId() {
        HelpVO helpVO = new HelpVO();
        Integer id = Context.getId();
        Integer supervisorId = adminMapper.getSupervisorIdByCounselor(id);
        
        // 使用新的查询方法获取督导-咨询师会话ID
        Integer consultId = chatMapper.getConsultSupervisorCounselor(supervisorId, id);
        
        helpVO.setSupervisorSessionId(consultId != null ? consultId.toString() : null);
        helpVO.setSupervisorId(supervisorId.toString());
        Admin admin = adminMapper.getAdminById(supervisorId);
        helpVO.setSupervisorName(admin.getAccountName());
        helpVO.setSupervisorAvatar(admin.getAvatar());
        return helpVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void setHelp(Integer supervisorId, Integer counselorId) {
        try {
            log.info("开始绑定督导({})和咨询师({})的关系", supervisorId, counselorId);
            
            // 1.检查关系是否已存在
            Integer exists = adminMapper.checkManageExists(supervisorId, counselorId);
            if (exists != null && exists > 0) {
                log.info("管理关系已经存在，不再重复创建");
                return;
            }
            
            // 2.建立帮助关系
            log.info("创建管理关系：督导({}) -> 咨询师({})", supervisorId, counselorId);
            adminMapper.setManage(supervisorId, counselorId);
            log.info("管理关系创建成功");

            // 3.更新督导的info字段，添加咨询师到supervisees
            log.info("开始更新督导({})的supervisees列表", supervisorId);
            updateSupervisorInfo(supervisorId, counselorId, true);
            log.info("督导supervisees列表更新完成");
            
            // 4.更新咨询师的info字段，添加supervisorId和supervisorName
            log.info("开始更新咨询师({})的supervisor信息", counselorId);
            Admin supervisor = adminMapper.getAdminById(supervisorId);
            updateCounselorInfo(counselorId, supervisorId, supervisor.getAccountName(), true);
            log.info("咨询师supervisor信息更新完成");

            // 5.检查会话是否已存在
            Integer consultExists = chatMapper.checkSupervisorConsultExists(counselorId, supervisorId);
            if (consultExists != null && consultExists > 0) {
                log.info("会话已经存在，不再重复创建");
                return;
            }

            // 6.建立会话
            try {
                log.info("创建会话记录：督导({}) -> 咨询师({})", supervisorId, counselorId);
                chatMapper.addConsultAdminAdmin(supervisorId, counselorId);
                log.info("会话记录创建成功");
            } catch (Exception e) {
                log.error("创建会话记录失败：{}", e.getMessage(), e);
                // 如果是唯一约束冲突等可忽略的错误，不抛出异常
                if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
                    log.info("会话记录已存在，忽略错误");
                } else {
                    throw e; // 重新抛出其他类型的异常
                }
            }
            
            log.info("成功完成督导({})和咨询师({})的绑定", supervisorId, counselorId);
        } catch (Exception e) {
            log.error("绑定督导和咨询师关系时发生错误: {}", e.getMessage(), e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void removeHelp(Integer supervisorId, Integer counselorId) {
        try {
            log.info("开始解除督导({})和咨询师({})的关系", supervisorId, counselorId);
            
            //1.检查关系是否存在
            Integer exists = adminMapper.checkManageExists(supervisorId, counselorId);
            if (exists == null || exists == 0) {
                log.info("管理关系不存在，无需删除");
            } else {
                //2.移除管理关系
                log.info("移除管理关系：督导({}) -> 咨询师({})", supervisorId, counselorId);
                adminMapper.removeManage(supervisorId, counselorId);
                log.info("管理关系移除成功");
                
                //3.更新督导的info字段，从supervisees中移除咨询师
                log.info("开始从督导({})的supervisees列表中移除咨询师({})", supervisorId, counselorId);
                updateSupervisorInfo(supervisorId, counselorId, false);
                log.info("督导supervisees列表更新完成");
                
                //4.更新咨询师的info字段，移除supervisorId和supervisorName
                log.info("开始从咨询师({})中移除supervisor信息", counselorId);
                updateCounselorInfo(counselorId, supervisorId, null, false);
                log.info("咨询师supervisor信息更新完成");
            }
            
            //5.检查会话是否存在
            Integer consultExists = chatMapper.checkSupevisorConsultExists(counselorId, supervisorId);
            if (consultExists == null || consultExists == 0) {
                log.info("会话不存在，无需删除");
            } else {
                //6.移除会话
                try {
                    log.info("移除会话记录：督导({}) -> 咨询师({})", supervisorId, counselorId);
                    chatMapper.removeSupervisorConsult(counselorId, supervisorId);
                    log.info("会话记录移除成功");
                } catch (Exception e) {
                    log.error("移除会话记录失败：{}", e.getMessage(), e);
                    throw e; // 重新抛出异常
                }
            }
            
            log.info("成功完成解除督导({})和咨询师({})的关系", supervisorId, counselorId);
        } catch (Exception e) {
            log.error("解除督导和咨询师关系时发生错误: {}", e.getMessage(), e);
            throw e; // 重新抛出异常以触发事务回滚
        }
    }
    
    /**
     * 更新督导的info字段中的supervisees列表
     * @param supervisorId 督导ID
     * @param counselorId 咨询师ID
     * @param isAdd 是添加还是删除
     */
    private void updateSupervisorInfo(Integer supervisorId, Integer counselorId, boolean isAdd) {
        try {
            // 获取督导当前的info - 确保获取最新的数据
            String infoJson = adminMapper.getAdminInfo(supervisorId);
            log.info("获取督导({})当前info: {}", supervisorId, infoJson);
            
            if (infoJson == null || infoJson.isEmpty()) {
                // 如果info为空，创建一个新的包含supervisees的对象
                Map<String, Object> newInfo = new HashMap<>();
                List<String> supervisees = new ArrayList<>();
                if (isAdd) {
                    supervisees.add(counselorId.toString());
                }
                newInfo.put("supervisees", supervisees);
                String newInfoJson = objectMapper.writeValueAsString(newInfo);
                log.info("督导({})的info为空，创建新的info: {}", supervisorId, newInfoJson);
                adminMapper.updateAdminInfo(supervisorId, newInfoJson);
                log.info("更新督导info成功");
                return;
            }
            
            // 解析现有的info
            Map<String, Object> infoMap = objectMapper.readValue(infoJson, new TypeReference<Map<String, Object>>() {});
            
            // 获取或创建supervisees列表
            List<String> supervisees = new ArrayList<>(); // 创建新的列表实例
            if (infoMap.containsKey("supervisees")) {
                Object superviseeObj = infoMap.get("supervisees");
                log.info("原supervisees对象类型: {}", superviseeObj != null ? superviseeObj.getClass().getName() : "null");
                
                if (superviseeObj instanceof List) {
                    // 逐个转换元素，确保类型安全，复制到新列表
                    ((List<?>) superviseeObj).forEach(item -> {
                        if (item != null) {
                            supervisees.add(item.toString());
                        }
                    });
                    log.info("从现有列表获取supervisees: {}", supervisees);
                } else {
                    log.info("supervisees不是List类型，使用新的空列表");
                }
            } else {
                log.info("info中不存在supervisees字段，创建新的空列表");
            }
            
            // 根据操作添加或删除counselorId
            String counselorIdStr = counselorId.toString();
            if (isAdd) {
                // 添加操作，确保不重复添加
                if (!supervisees.contains(counselorIdStr)) {
                    supervisees.add(counselorIdStr);
                    log.info("添加咨询师ID {} 到督导 {} 的supervisees列表", counselorIdStr, supervisorId);
                } else {
                    log.info("咨询师ID {} 已在督导 {} 的supervisees列表中", counselorIdStr, supervisorId);
                }
            } else {
                // 删除操作
                if (supervisees.contains(counselorIdStr)) {
                    supervisees.remove(counselorIdStr);
                    log.info("从督导 {} 的supervisees列表中移除咨询师ID {}", supervisorId, counselorIdStr);
                    log.info("移除后的supervisees列表: {}", supervisees);
                } else {
                    log.info("咨询师ID {} 不在督导 {} 的supervisees列表中，无需移除", counselorIdStr, supervisorId);
                }
            }
            
            // 确保将修改后的supervisees列表重新放入map
            infoMap.put("supervisees", supervisees);
            
            // 更新info字段
            String updatedInfoJson = objectMapper.writeValueAsString(infoMap);
            log.info("准备更新督导({})的info为: {}", supervisorId, updatedInfoJson);
            adminMapper.updateAdminInfo(supervisorId, updatedInfoJson);
            log.info("督导({})的info更新成功", supervisorId);
            
        } catch (JsonProcessingException e) {
            log.error("处理督导info字段时出错: {}", e.getMessage(), e);
            throw new RuntimeException("更新督导信息失败", e);
        }
    }

    /**
     * 更新咨询师的info字段中的supervisor信息
     * @param counselorId 咨询师ID
     * @param supervisorId 督导ID
     * @param supervisorName 督导名称
     * @param isAdd 是添加还是删除
     */
    private void updateCounselorInfo(Integer counselorId, Integer supervisorId, String supervisorName, boolean isAdd) {
        try {
            // 获取咨询师当前的info
            String infoJson = adminMapper.getAdminInfo(counselorId);
            log.info("获取咨询师({})当前info: {}", counselorId, infoJson);
            
            Map<String, Object> infoMap;
            if (infoJson == null || infoJson.isEmpty()) {
                // 如果info为空，创建一个新的对象
                infoMap = new HashMap<>();
            } else {
                // 解析现有的info
                infoMap = objectMapper.readValue(infoJson, new TypeReference<Map<String, Object>>() {});
            }
            
            if (isAdd) {
                // 添加督导信息
                infoMap.put("supervisorId", supervisorId.toString());
                infoMap.put("supervisorName", supervisorName);
                log.info("向咨询师({})添加督导信息: ID={}, Name={}", counselorId, supervisorId, supervisorName);
            } else {
                // 移除督导信息
                infoMap.remove("supervisorId");
                infoMap.remove("supervisorName");
                log.info("从咨询师({})移除督导信息", counselorId);
            }
            
            // 更新info字段
            String updatedInfoJson = objectMapper.writeValueAsString(infoMap);
            log.info("准备更新咨询师({})的info为: {}", counselorId, updatedInfoJson);
            adminMapper.updateAdminInfo(counselorId, updatedInfoJson);
            log.info("咨询师({})的info更新成功", counselorId);
            
        } catch (JsonProcessingException e) {
            log.error("处理咨询师info字段时出错: {}", e.getMessage(), e);
            throw new RuntimeException("更新咨询师信息失败", e);
        }
    }

    @Override
    public List<SelectUserVO> getAllUser() {
        List<User> users = userMapper.selectAllUser();
        List<SelectUserVO> userVOS = new ArrayList<>();
        for (User user : users) {
            SelectUserVO selectUserVO = new SelectUserVO();
            selectUserVO.setId(user.getUserId().toString());
            selectUserVO.setName(user.getName());
            selectUserVO.setEmail(user.getEmail());
            selectUserVO.setPhone(user.getPhone());
            selectUserVO.setAvatar(user.getAvatar());
            selectUserVO.setAge(user.getAge());
            selectUserVO.setStatus(user.getStatus());
            selectUserVO.setGender(user.getGender());
            userVOS.add(selectUserVO);
        }
        return userVOS;
    }

    @Override
    public SelectUserVO getAllUserById(Integer userId) {
        User user = userMapper.selectUserById(userId);
        SelectUserVO selectUserVO = new SelectUserVO();
        selectUserVO.setId(user.getUserId().toString());
        selectUserVO.setName(user.getName());
        selectUserVO.setEmail(user.getEmail());
        selectUserVO.setPhone(user.getPhone());
        selectUserVO.setAvatar(user.getAvatar());
        selectUserVO.setAge(user.getAge());
        selectUserVO.setStatus(user.getStatus());
        selectUserVO.setGender(user.getGender());
        return selectUserVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setHelp(Integer counselorId) {
        try {
            Integer supervisorId = Context.getId();
            log.info("当前督导ID: {}, 准备与咨询师ID: {} 建立关系", supervisorId, counselorId);
            setHelp(supervisorId, counselorId);
        } catch (Exception e) {
            log.error("设置督导与咨询师关系时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeHelp(Integer counselorId) {
        try {
            Integer supervisorId = Context.getId();
            log.info("当前督导ID: {}, 准备解除与咨询师ID: {} 的关系", supervisorId, counselorId);
            removeHelp(supervisorId, counselorId);
        } catch (Exception e) {
            log.error("解除督导与咨询师关系时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public SelectAdminVO getAdminById(Integer adminId) {
        Admin admin = adminMapper.getAdminById(adminId);
        if (admin == null) {
            log.error("未找到ID为{}的管理员", adminId);
            throw new BadRequestException("管理员不存在");
        }
        
        return SelectAdminVO.builder()
                .id(admin.getAdminId().toString())
                .username(admin.getAccountName())
                .name(admin.getRealName())
                .phone(admin.getPhone())
                .avatar(admin.getAvatar())
                .email(admin.getEmail())
                .profile(admin.getInfo())
                .role(admin.getRole())
                .createdAt(admin.getCreatedAt().toString())
                .build();
    }

}
