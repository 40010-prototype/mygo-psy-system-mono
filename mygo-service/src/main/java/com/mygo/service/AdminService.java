package com.mygo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.dto.AdminLoginDTO;
import com.mygo.dto.AdminRegisterDTO;
import com.mygo.dto.ResetPasswordDTO;
import com.mygo.vo.AdminInfoVO;
import com.mygo.vo.AdminLoginVO;

public interface AdminService {

    AdminLoginVO login(AdminLoginDTO adminLoginDTO) throws JsonProcessingException;

    void register(AdminRegisterDTO adminRegisterDTO) throws JsonProcessingException;

    String sendEmail(String name);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    AdminInfoVO getAdminInfo();

}
