package com.mygo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.dto.*;
import com.mygo.enumeration.Role;
import com.mygo.vo.*;

import java.util.Date;
import java.util.List;

public interface AdminService {

    AdminLoginVO login(AdminLoginDTO adminLoginDTO) throws JsonProcessingException;

    void register(AdminRegisterDTO adminRegisterDTO) throws JsonProcessingException;

    String sendEmail(String name);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    AdminInfoVO getAdminInfo();

    List<AdminSessionVO> getSession();

    List<AdminMessageVO> getMessages(Integer sessionId, Integer limit, Integer offset) throws JsonProcessingException;

    void read(Integer sessionId);

    void addSchedule(AdminAddScheduleDTO adminAddScheduleDTO);

    void approveScheduleByDay(Integer scheduleId);

    void approveScheduleByTimeSlot(Integer timeSlotId);

    AdminScheduleVO getScheduleByCounselor(Date startDate, Date endDate);

    List<AdminScheduleVO> getScheduleBySupervisor(Date startDate, Date endDate);

    List<AdminScheduleVO> getScheduleByManager(Date startDate, Date endDate);

    List<AdminScheduleVO> getPendingScheduleBySupervisor(Date startDate, Date endDate);

    List<SelectAdminVO> getAllAdminByRole(Role role);

    HelpVO getHelpSessionId();

    void setHelp(Integer counselorId);

    void removeHelp(Integer counselorId);

    void setHelp(Integer supervisorId, Integer counselorId);

    void removeHelp(Integer supervisorId, Integer counselorId);

    List<SelectUserVO> getAllUser();

    SelectUserVO getAllUserById(Integer userId);

    SelectAdminVO getAdminById(Integer adminId);

}


