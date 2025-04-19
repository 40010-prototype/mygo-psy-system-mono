package com.mygo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.dto.AdminLoginDTO;
import com.mygo.dto.AdminRegisterDTO;
import com.mygo.dto.ResetPasswordDTO;
import com.mygo.vo.AdminMessageVO;
import com.mygo.vo.AdminInfoVO;
import com.mygo.vo.AdminLoginVO;
import com.mygo.vo.AdminSessionVO;

import java.util.List;

public interface AdminService {

    AdminLoginVO login(AdminLoginDTO adminLoginDTO) throws JsonProcessingException;

    void register(AdminRegisterDTO adminRegisterDTO) throws JsonProcessingException;

    String sendEmail(String name);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    AdminInfoVO getAdminInfo();

    List<AdminSessionVO> getSession();

    List<AdminMessageVO> getMessages(Integer sessionId, Integer limit, Integer offset) throws JsonProcessingException;

}
