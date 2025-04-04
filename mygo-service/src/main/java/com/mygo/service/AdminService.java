package com.mygo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.domain.dto.AdminLoginDTO;
import com.mygo.domain.dto.AdminRegisterDTO;
import com.mygo.domain.dto.ResetPasswordDTO;
import com.mygo.domain.dto.UserDTO;
import com.mygo.domain.vo.AdminLoginVO;
import com.mygo.result.Result;

public interface AdminService {

    AdminLoginVO login(AdminLoginDTO adminLoginDTO) throws JsonProcessingException;

    void register(AdminRegisterDTO adminRegisterDTO);

    String sendEmail(String name);

    void resetPassword(ResetPasswordDTO resetPasswordDTO);

    Result<UserDTO> getUserInfo();

}
