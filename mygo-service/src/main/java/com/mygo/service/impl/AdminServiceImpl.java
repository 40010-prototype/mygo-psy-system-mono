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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
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
            log.info(String.valueOf(consult.getUserId()));
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
        Integer consultId = chatMapper.getConsultId(id, supervisorId);
        helpVO.setSupervisorSessionId(consultId.toString());
        helpVO.setSupervisorId(supervisorId.toString());
        Admin admin = adminMapper.getAdminById(supervisorId);
        helpVO.setSupervisorName(admin.getAccountName());
        helpVO.setSupervisorAvatar(admin.getAvatar());
        return helpVO;

    }

    @Override
    public void setHelp(Integer counselorId) {
        Integer supervisorId = Context.getId();
        setHelp(supervisorId, counselorId);
    }

    @Override
    public void removeHelp(Integer counselorId) {
        Integer supervisorId = Context.getId();
        removeHelp(supervisorId, counselorId);
    }
    
    @Override
    public void setHelp(Integer supervisorId, Integer counselorId) {
        //1.检查关系是否已存在
        Integer exists = adminMapper.checkManageExists(supervisorId, counselorId);
        if (exists != null && exists > 0) {
            log.info("管理关系已经存在，不再重复创建");
            return;
        }
        
        //2.建立帮助关系
        adminMapper.setManage(supervisorId, counselorId);
        
        //3.检查会话是否已存在
        Integer consultExists = chatMapper.checkConsultExists(supervisorId, counselorId);
        if (consultExists != null && consultExists > 0) {
            log.info("会话已经存在，不再重复创建");
            return;
        }
        
        //4.建立会话
        chatMapper.addConsult(supervisorId, counselorId);
    }
    
    @Override
    public void removeHelp(Integer supervisorId, Integer counselorId) {
        //1.检查关系是否存在
        Integer exists = adminMapper.checkManageExists(supervisorId, counselorId);
        if (exists == null || exists == 0) {
            log.info("管理关系不存在，无需删除");
        } else {
            //2.移除管理关系
            adminMapper.removeManage(supervisorId, counselorId);
        }
        
        //3.检查会话是否存在
        Integer consultExists = chatMapper.checkConsultExists(supervisorId, counselorId);
        if (consultExists == null || consultExists == 0) {
            log.info("会话不存在，无需删除");
        } else {
            //4.移除会话
            chatMapper.removeConsult(supervisorId, counselorId);
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

}
